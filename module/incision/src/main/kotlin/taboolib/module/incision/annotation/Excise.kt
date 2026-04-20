package taboolib.module.incision.annotation

/**
 * 覆写式 advice。
 *
 * 用途：
 * 直接替换整段目标方法，等价于 Mixin `@Overwrite`。
 *
 * 使用：
 * 把注解标在 `@Surgeon` object 的方法上，通过 [scope] 指定唯一目标。
 * handler 签名应与目标方法兼容，包括 self 与原始参数。
 *
 * 效果：
 * 命中后原方法体不再执行，所有行为由 handler 接管。
 *
 * 局限：
 * 1. 同一目标只允许一个 [Excise]，多于一个会触发 `Trauma.Conflict.MultipleExcise`。
 * 2. 因为直接替换整段方法，兼容性和风险都高于入口/出口类 advice。
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Excise(
    /** 目标选择器 DSL。 */
    val scope: String,

    /** 额外的指令序列约束，用于限制只对满足特定字节码形态的目标方法生效。 */
    val pattern: InsnPattern = InsnPattern([]),

    /** 命名标签，可供 DSL、诊断输出或后续匹配事件引用。 */
    val where: String = "",
)
