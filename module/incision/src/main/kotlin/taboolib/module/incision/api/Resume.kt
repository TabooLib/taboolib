package taboolib.module.incision.api

/**
 * 放行原指令的句柄，由 Splice / Around 类切术使用。
 *
 * 调用 [proceed] 会按链式顺序触发下一条 advice，最终触发原方法。
 * 调用 [proceedResult] 可把一个结果继续传给下游 advice，而不立刻终止整条链。
 * 调用 [skip] 跳过下游 advice 直接终止，并使用给定值作为最终返回。
 */
interface Resume {

    /** 放行至下游 advice 或原方法，返回链式调用结果 */
    fun proceed(): Any?

    /** 放行并替换实参 */
    fun proceed(vararg args: Any?): Any?

    /** 使用新的当前结果继续传给下游 advice */
    fun proceedResult(returnValue: Any?): Any?

    /** 跳过原方法，使用给定返回值终止链 */
    fun skip(returnValue: Any?): Any?

    /** 是否已显式放行过 */
    val proceeded: Boolean
}
