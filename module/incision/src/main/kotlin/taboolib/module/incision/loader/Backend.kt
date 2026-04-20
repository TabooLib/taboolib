package taboolib.module.incision.loader

/**
 * 织入后端 — 负责让 weaver 的字节码生效。
 *
 * 三种后端：
 * - [InstrumentationBackend] 主力：通过 java.lang.instrument 跨 ClassLoader retransform
 * - [ClassLoaderHookBackend] 兜底：拦截 PluginClassLoader.findClass（仅命中后续加载）
 * - [PipelineBackend] NMSProxy 兼容：接入 TabooLib RemapTranslation 的额外 transformer 链
 */
interface Backend {

    val name: String

    fun available(): Boolean

    /** 为指定类名注册 transformer；返回可用于取消的 token */
    fun addTransformer(className: String, transformer: (ByteArray) -> ByteArray?): BackendToken

    /** 立即触发一次 retransform（若支持） */
    fun retransform(className: String): Boolean = false

    interface BackendToken {
        fun remove()
    }
}
