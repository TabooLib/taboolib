package taboolib.module.incision.annotation

/**
 * 植入式 advice。
 *
 * 用途：
 * 在指定锚点前后追加一段逻辑，而不替换原始指令，适合做探针、记录和轻量联动。
 *
 * 使用：
 * 通过 [method] 锁定宿主方法，通过 [site] 选择要植入的位置，例如 INVOKE、FIELD_GET、
 * FIELD_PUT、NEW 等。
 *
 * 效果：
 * 命中后会在选定位置额外插入一次 handler 调用，原始指令仍会继续执行。
 *
 * 局限：
 * 1. `@Graft` 擅长“追加”，不负责替换或阻断原调用。
 * 2. 锚点选择错误时很容易出现未命中或执行次数偏差。
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Graft(

    /** 宿主方法描述符，格式为 `类#方法名(参数)返回`。 */
    val method: String,

    /** 植入位置。 */
    val site: Site,

    /** 额外的指令序列约束，用于在同一宿主方法中进一步缩小命中范围。 */
    val pattern: InsnPattern = InsnPattern([]),

    /** 命名标签，可供 DSL、诊断输出或后续匹配事件引用。 */
    val where: String = "",
)
