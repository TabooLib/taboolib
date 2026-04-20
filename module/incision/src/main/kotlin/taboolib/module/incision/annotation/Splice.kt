package taboolib.module.incision.annotation

/**
 * 环绕 advice。
 *
 * 用途：
 * 完整包裹原方法，适合做“先判断再决定是否放行”的逻辑，例如权限判断、参数改写、
 * 返回值接管、短路返回等。
 *
 * 使用：
 * handler 常见签名为 `fun(theatre: Theatre): Any?`。命中后必须显式调用
 * `theatre.resume.proceed()`、`theatre.resume.proceed(args...)`、
 * `theatre.resume.proceedResult(...)` 或 `theatre.override(...)` 之一。
 *
 * 效果：
 * `@Splice` 能同时观察调用前、调用中和调用后，是控制力最强的 advice 形式。
 *
 * 局限：
 * 1. 没有显式 resume/override 会触发 `Trauma.Runtime.ResumeMissing`。
 * 2. 控制面越大，越需要确保 handler 与目标返回类型、异常语义保持一致。
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Splice(
    /** 目标选择器 DSL。 */
    val scope: String,

    /** 额外的指令序列约束，用于限制只对满足特定字节码形态的目标方法生效。 */
    val pattern: InsnPattern = InsnPattern([]),

    /** 命名标签，可供 DSL、诊断输出或后续匹配事件引用。 */
    val where: String = "",
)
