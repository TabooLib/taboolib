package taboolib.module.incision.dsl

import taboolib.module.incision.api.MethodCoordinate
import taboolib.module.incision.api.Suture
import taboolib.module.incision.runtime.AdviceEntry
import taboolib.module.incision.runtime.AdviceKind
import taboolib.module.incision.runtime.SurgeryRegistry
import taboolib.module.incision.runtime.TheatreDispatcher
import kotlin.reflect.KClass

/**
 * Suture 默认实现 — 持有一组 advice 条目；heal 时从 dispatcher 移除。
 */
internal class SutureImpl(
    override val id: String,
    override val targets: List<MethodCoordinate>,
    override val holder: KClass<*>,
    private val entries: List<AdviceEntry>,
) : Suture {

    @Volatile
    override var state: Suture.State = Suture.State.ARMED

    override fun heal(): Boolean {
        if (state == Suture.State.HEALED) return false
        for (e in entries) {
            TheatreDispatcher.unregister(e.target, e.id)
            // 清理自动扩展的 $Companion 条目
            if (!e.target.owner.endsWith("\$Companion")) {
                val companionTarget = e.target.copy(owner = e.target.owner + "\$Companion")
                TheatreDispatcher.unregister(companionTarget, e.id + "#companion")
            }
        }
        SurgeryRegistry.unregister(id)
        state = Suture.State.HEALED
        return true
    }

    override fun suspend(): Boolean {
        if (state == Suture.State.HEALED) return false
        for (e in entries) e.enabled = false
        state = Suture.State.SUSPENDED
        return true
    }

    override fun resume(): Boolean {
        if (state == Suture.State.HEALED) return false
        for (e in entries) e.enabled = true
        state = Suture.State.ARMED
        return true
    }
}
