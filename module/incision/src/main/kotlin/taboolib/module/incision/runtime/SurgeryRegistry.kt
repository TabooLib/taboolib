package taboolib.module.incision.runtime

import taboolib.module.incision.api.MethodCoordinate
import taboolib.module.incision.api.Suture
import taboolib.module.incision.diagnostic.Forensics
import taboolib.module.incision.diagnostic.Trauma
import java.util.concurrent.ConcurrentHashMap

/**
 * 全局切术注册表。
 *
 * 持有 id → Suture 的映射，负责：
 * - 唯一性校验（DuplicateId）
 * - 按 holder / target / plugin 查询
 * - 卸载时从 dispatcher chain 中移除
 *
 * 同一 JVM 内有且仅有一个实例（本地层面；跨插件的全局协调由 IncisionGate 负责）。
 */
object SurgeryRegistry {

    private val byId = ConcurrentHashMap<String, Suture>()
    private val byTarget = ConcurrentHashMap<String, MutableList<Suture>>()

    fun register(id: String, suture: Suture) {
        val prev = byId.putIfAbsent(id, suture)
        if (prev != null) {
            throw Trauma.Declaration.DuplicateId(id, prev.javaClass.name)
        }
        for (t in suture.targets) {
            byTarget.computeIfAbsent(t.signature) { mutableListOf() }.add(suture)
        }
        Forensics.debug("register id=$id targets=${suture.targets}")
    }

    fun unregister(id: String): Boolean {
        val s = byId.remove(id) ?: return false
        for (t in s.targets) {
            byTarget[t.signature]?.remove(s)
        }
        return true
    }

    fun find(id: String): Suture? = byId[id]

    fun list(): Collection<Suture> = byId.values

    fun listByTarget(target: MethodCoordinate): List<Suture> = byTarget[target.signature].orEmpty().toList()

    fun clear() {
        byId.clear()
        byTarget.clear()
    }
}
