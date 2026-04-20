package taboolib.module.incision.loader

import taboolib.module.incision.diagnostic.Forensics
import java.util.concurrent.ConcurrentHashMap

/**
 * Pipeline 后端 — 供 TabooLib `RemapTranslation` / `AsmClassTranslation` 在
 * 完成自身 remap/require/dynamic 后调用 incision 的二次织入。
 *
 * 目前 TabooLib 并未公开 `extraTransformers` 挂钩点（见 PLAN 末的可选 PR），
 * 因此本后端提供以下接入方式：
 *
 * 1. **推式** — incision 调用方注册 transformer，此后任何调用 [apply] 的代码
 *    （未来由 NMSProxy 管线调用）都会按顺序应用所有注册的 transformer。
 * 2. **拉式** — TabooLib 自己在 NMSProxy 最终产物上调用 `PipelineBackend.apply(className, bytes)`
 *    即可把 incision 织入塞进去。
 *
 * 当 TabooLib 提供 hook 前，本后端也可由 [InstrumentationBackend] 兜底复用：
 * NMSProxy 的 impl 类一旦被加载，InstrumentationBackend 也能 retransform 它。
 */
object PipelineBackend : Backend {

    private val transformers = ConcurrentHashMap<String, MutableList<(ByteArray) -> ByteArray?>>()

    override val name: String = "Pipeline"
    override fun available(): Boolean = true

    override fun addTransformer(className: String, transformer: (ByteArray) -> ByteArray?): Backend.BackendToken {
        val key = className.replace('.', '/')
        transformers.computeIfAbsent(key) { mutableListOf() }.add(transformer)
        return object : Backend.BackendToken {
            override fun remove() { transformers[key]?.remove(transformer) }
        }
    }

    /** 供 TabooLib 管线末端调用 — 给定 className + 当前字节码，返回织入后的字节码。 */
    fun apply(className: String, bytes: ByteArray): ByteArray {
        val key = className.replace('.', '/')
        val list = transformers[key] ?: return bytes
        var cur = bytes
        for (t in list) {
            val out = try { t(cur) } catch (e: Throwable) {
                Forensics.error("PipelineBackend transformer 执行失败: $key", e); null
            }
            if (out != null) cur = out
        }
        return cur
    }

    fun has(className: String): Boolean = transformers.containsKey(className.replace('.', '/'))

    fun clear() = transformers.clear()

    /**
     * 把本后端注册进 TabooLib `RemapTranslation.extraTransformers`，
     * 使 NMSProxy 管线最后一环调用 incision 织入。
     *
     * `:module:bukkit-nms` 不在 classpath 时静默跳过；多次调用幂等。
     */
    fun installIntoTabooLib(): Boolean {
        return try {
            val cls = Class.forName("taboolib.module.nms.remap.RemapTranslation")
            val list = cls.getDeclaredField("extraTransformers").apply { isAccessible = true }.get(null)
                    as MutableList<(String, ByteArray) -> ByteArray?>
            // 用一个固定身份的 transformer，避免重复挂载
            val sentinel = HOOK
            if (list.any { it === sentinel }) return true
            list += sentinel
            Forensics.info("PipelineBackend installed into TabooLib RemapTranslation.extraTransformers")
            true
        } catch (_: ClassNotFoundException) {
            false
        } catch (t: Throwable) {
            Forensics.warn("PipelineBackend install failed: ${t.message}")
            false
        }
    }

    /** 固定 transformer 实例 — 路由到 [apply]，便于重复挂载检测。 */
    private val HOOK: (String, ByteArray) -> ByteArray? = { className, bytes ->
        if (!has(className)) null else apply(className, bytes)
    }
}
