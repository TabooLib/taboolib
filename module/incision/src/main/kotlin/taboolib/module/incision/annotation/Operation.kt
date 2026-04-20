package taboolib.module.incision.annotation

/**
 * 单条 advice 的运行元信息。
 *
 * 用途：
 * 在不改变 advice 主语义的前提下，补充执行优先级、默认启用状态和稳定 id。
 *
 * 使用：
 * 把它附加在任意 advice 方法上。若类级 [Surgeon] 已设置默认优先级，
 * [priority] 会作为方法级覆盖值。
 *
 * 效果：
 * - [priority] 越大越先执行
 * - [enabled] 为 `false` 时默认不启用，需要后续手动 resume
 * - [id] 用于稳定识别、排错和管理该 operation
 *
 * 局限：
 * 1. 它只描述元信息，不改变 advice 本身的 handler 签名要求。
 * 2. 如果显式 [id] 管理不当，仍可能造成命名冲突或诊断困难。
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Operation(

    /** 方法级优先级覆盖；数值越大越先执行。 */
    val priority: Int = 0,

    /** 是否默认启用；若为 `false`，后续需通过 `Suture.resume()` 手动启用。 */
    val enabled: Boolean = true,

    /** 显式 operation id 后缀；未指定时默认使用方法名。 */
    val id: String = "",
)
