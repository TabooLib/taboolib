package taboolib.module.incision.lifecycle

import taboolib.module.incision.diagnostic.Forensics
import taboolib.module.incision.runtime.SurgeryRegistry
import taboolib.module.incision.runtime.TheatreDispatcher

/**
 * 自动卸载 — 当某 ClassLoader 被释放或某插件 disable 时，从 dispatcher 链中移除
 * 该 ClassLoader 拥有的全部 advice。
 *
 * 接入方式：
 * - 调用方在自己的 PluginDisableEvent 监听器里调用 [healByPlugin]
 * - 或在 Awake DISABLE 阶段调用 [healByClassLoader] 传入本插件 ClassLoader
 *
 * incision 不直接订阅 Bukkit 事件，避免对 platform 模块的硬依赖。
 */
object AutoHealHandler {

    /** 卸载某 ClassLoader 上注册的全部 advice，返回卸载条目数。 */
    fun healByClassLoader(cl: ClassLoader): Int {
        var n = 0
        // 复制以避免并发改动
        val sutures = SurgeryRegistry.list().toList()
        for (s in sutures) {
            val targets = s.targets.toList()
            var anyMatched = false
            for (t in targets) {
                val chain = TheatreDispatcher.chainOf(t)
                val before = chain.list().size
                val removed = chain.removeByClassLoader(cl)
                n += removed
                if (removed > 0 && removed == before) anyMatched = true
            }
            if (anyMatched) {
                s.heal()
            }
        }
        if (n > 0) Forensics.info("AutoHeal: classLoader=${cl} removed=$n advice")
        return n
    }

    /** 按插件名前缀卸载（id 以 plugin 包名为前缀）。 */
    fun healByPlugin(pluginPackagePrefix: String): Int {
        var n = 0
        val sutures = SurgeryRegistry.list().filter { it.id.startsWith(pluginPackagePrefix) }.toList()
        for (s in sutures) if (s.heal()) n++
        if (n > 0) Forensics.info("AutoHeal: pluginPrefix=$pluginPackagePrefix removed=$n suture")
        return n
    }
}
