package taboolib.module.incision.annotation

/**
 * 尾随 advice。
 *
 * 用途：
 * 在目标方法所有出口处执行收尾逻辑，适合做日志、计数、清理和返回值观察。
 *
 * 使用：
 * 把注解标在 `@Surgeon` object 的方法上，通过 [scope] 指定目标；
 * 如需覆盖异常出口，保持 [onThrow] 为 `true`。
 *
 * 效果：
 * advice 会在 `return` 之前或异常即将抛出时触发，可通过 `Theatre.throwable`
 * 区分正常返回与异常出口。
 *
 * 局限：
 * 1. 它关注的是“离开目标方法”的时刻，不适合表达方法中段的局部锚点。
 * 2. 如果关闭 [onThrow]，异常出口不会执行该 advice。
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Trail(
    /** 目标选择器 DSL。 */
    val scope: String,

    /** 是否同时覆盖异常出口；为 `false` 时仅在正常返回时触发。 */
    val onThrow: Boolean = true,

    /** 额外的指令序列约束，用于限制只在特定字节码结构的目标方法上生效。 */
    val pattern: InsnPattern = InsnPattern([]),

    /** 命名标签，可供 DSL、诊断输出或后续匹配事件引用。 */
    val where: String = "",
)
