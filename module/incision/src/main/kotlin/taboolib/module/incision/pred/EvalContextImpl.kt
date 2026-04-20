package taboolib.module.incision.pred

import taboolib.module.incision.api.MethodCoordinate
import taboolib.module.incision.api.Theatre

/**
 * 基于 [Theatre] 的 [EvalContext] 实现，每次 advice 调用前由 dispatcher 构造并复用。
 *
 * 懒求值字段（`callerCache`）在第一次访问时才计算；其他字段直接代理 Theatre。
 *
 * 不可跨线程复用：dispatcher 为每次调用 new 一个实例。如需池化由调用方在 reset 时
 * 清掉 [callerCache]。
 */
internal class EvalContextImpl(
    private val theatre: Theatre,
    private val coord: MethodCoordinate,
    private val resultProvider: () -> Any?,
    private val envMap: Map<String, Any?> = emptyMap(),
) : EvalContext {

    private object UNSET
    private var callerCache: Any? = UNSET

    override fun argAt(i: Int): Any? = theatre.args[i]

    override fun argCount(): Int = theatre.args.size

    override fun thisRef(): Any? = theatre.self

    override fun result(): Any? = resultProvider()

    override fun env(): Map<String, Any?> = envMap

    override fun site(): Any? = coord

    override fun caller(): Any? {
        if (callerCache !== UNSET) return callerCache
        // 跳过 Throwable / EvalContextImpl / dispatcher / weaver 自身的栈帧
        val st = Throwable().stackTrace
        var found: StackTraceElement? = null
        for (e in st) {
            val cn = e.className
            if (cn.startsWith("taboolib.module.incision.")) continue
            if (cn.startsWith("io.izzel.incision.")) continue
            if (cn.startsWith("java.lang.")) continue
            found = e
            break
        }
        callerCache = found
        return found
    }
}
