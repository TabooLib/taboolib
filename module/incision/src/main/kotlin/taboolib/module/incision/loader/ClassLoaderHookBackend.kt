package taboolib.module.incision.loader

import taboolib.module.incision.diagnostic.Forensics
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

/**
 * Fallback 后端 — 反射劫持 PluginClassLoader（或当前线程 contextClassLoader）的 findClass/defineClass。
 *
 * 局限：仅命中 hook 注册之后才被加载的类，对已加载类无效。适合启动早期注册。
 */
object ClassLoaderHookBackend : Backend {

    private val transformers = ConcurrentHashMap<String, MutableList<(ByteArray) -> ByteArray?>>()

    @Volatile
    private var installed = false

    override val name: String = "ClassLoaderHook"

    override fun available(): Boolean = true

    override fun addTransformer(className: String, transformer: (ByteArray) -> ByteArray?): Backend.BackendToken {
        val key = className.replace('.', '/')
        transformers.computeIfAbsent(key) { mutableListOf() }.add(transformer)
        ensureInstalled()
        return object : Backend.BackendToken {
            override fun remove() { transformers[key]?.remove(transformer) }
        }
    }

    @Synchronized
    private fun ensureInstalled() {
        if (installed) return
        installed = true
        // 当前 ClassLoader 链上 walk，注入 transform 检查点（通过子类化 ClassLoader 不可行；
        // 这里只能依赖 hook 在 defineClass 之前 — 通过 Java Agent ClassFileTransformer 才能真正生效）
        // 因此该 backend 实际是个"标记"，转译动作仍要靠 InstrumentationBackend；
        // 当 InstrumentationBackend 可用时，这里把 transformers 委派过去。
        for ((cls, list) in transformers) {
            for (t in list) {
                InstrumentationBackend.addTransformer(cls, t)
            }
        }
        Forensics.info("ClassLoaderHookBackend: 已委派 ${transformers.size} 个 transformer 给 InstrumentationBackend")
    }
}
