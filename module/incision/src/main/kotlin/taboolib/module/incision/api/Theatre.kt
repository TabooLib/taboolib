package taboolib.module.incision.api

/**
 * 手术现场上下文 — 暴露给 advice handler 的唯一入口。
 *
 * 涵盖目标方法的实参、自身实例、目标方法坐标，以及对原指令的放行控制。
 */
interface Theatre {

    /** 目标方法坐标 */
    val target: MethodCoordinate

    /** 调用方实例（静态方法时为 null） */
    val self: Any?

    /** 实参数组（可读写；直接修改会反映到放行的调用） */
    val args: Array<Any?>

    /** Resume 句柄；@Lead/@Trail 中调用会被忽略并标记为非法 */
    val resume: Resume

    /**
     * 强制覆盖返回值并终止后续 advice 与原方法。
     * 等价于 `resume.skip(value)`。
     */
    fun override(value: Any?): Any? = resume.skip(value)

    /** 当前是否处于异常出口（@Trail 中可读） */
    val throwable: Throwable?
}
