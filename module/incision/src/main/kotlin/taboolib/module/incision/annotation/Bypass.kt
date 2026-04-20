package taboolib.module.incision.annotation

/**
 * 重定向 advice。
 *
 * 用途：
 * 把目标方法中的单条调用指令替换成自定义 handler，语义上接近 Mixin `@Redirect`。
 *
 * 使用：
 * 通过 [method] 指定宿主方法，通过 [site] 指定要替换的调用点或字段访问点。
 * handler 需要和被替换的调用位点在参数与返回值语义上保持兼容。
 *
 * 效果：
 * 命中后原位点不会再执行原始调用，而是改为执行你的 handler。
 *
 * 局限：
 * 1. 它只替换一个中段位点，不负责整个方法体的控制流接管。
 * 2. 如果 [site] 选得不精确，可能出现未命中或误命中的问题。
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Bypass(

    /** 宿主方法描述符，格式为 `类#方法名(参数)返回`。 */
    val method: String,

    /** 被替换的目标指令锚点。 */
    val site: Site,

    /** 额外的指令序列约束，用于在同一宿主方法中进一步缩小命中范围。 */
    val pattern: InsnPattern = InsnPattern([]),

    /** 命名标签，可供 DSL、诊断输出或后续匹配事件引用。 */
    val where: String = "",
)
