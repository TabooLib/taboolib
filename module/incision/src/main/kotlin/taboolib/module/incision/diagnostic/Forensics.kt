package taboolib.module.incision.diagnostic

/**
 * 结构化诊断日志器。
 *
 * 所有 incision 内部日志都走该入口，固定字段格式便于 grep:
 * `[Incision][<Phase>] id=... target=... resolver=... result=... took=...ms`
 *
 * 当前为最小实现 — 直接 println；后续接入 TabooLib `info/warning/severe`。
 */
object Forensics {

    /** 是否开启调试输出 — 通过 JVM 参数 -Dtaboolib.incision.debug=true 或环境变量 INCISION_DEBUG=1 启用 */
    val DEBUG: Boolean by lazy {
        System.getProperty("taboolib.incision.debug")?.equals("true", true) == true
                || System.getenv("INCISION_DEBUG") == "1"
    }

    fun info(message: String) {
        println("[Incision] $message")
    }

    fun debug(message: String) {
        if (DEBUG) println("[Incision][DEBUG] $message")
    }

    fun warn(message: String) {
        println("[Incision][WARN] $message")
    }

    fun error(message: String, cause: Throwable? = null) {
        System.err.println("[Incision][ERROR] $message")
        cause?.printStackTrace(System.err)
    }

    /**
     * 上报 Trauma — 输出结构化字段 + 触发栈。
     */
    fun report(trauma: Trauma) {
        System.err.println(buildString {
            append("[Incision][Trauma][").append(trauma.phase).append("] ")
            trauma.incisionId?.let { append("id=").append(it).append(' ') }
            trauma.target?.let { append("target=").append(it).append(' ') }
            trauma.surgeonClass?.let { append("surgeon=").append(it).append(' ') }
            trauma.resolverName?.let { append("resolver=").append(it).append(' ') }
            append("\n  reason: ").append(trauma.message)
            var c = trauma.cause
            while (c != null) {
                append("\n  cause : ").append(c)
                c = c.cause
            }
        })
    }
}
