package taboolib.module.incision.annotation

import taboolib.module.incision.api.Anchor

/**
 * 值改写 advice。
 *
 * 用途：
 * 修改某个位置正在流动的值，包括方法参数、返回值和局部变量。
 *
 * 使用：
 * 通过 [method] 指定宿主方法，通过 [kind] 指定要改写的是参数、返回值还是局部变量；
 * 当 [kind] 为参数或局部变量时，再用 [index] 指定具体槽位。
 *
 * 效果：
 * 命中后 handler 返回的新值会覆盖原值，从而改变后续执行结果。
 *
 * 局限：
 * 1. 需要保证返回值类型与被改写位点兼容。
 * 2. 局部变量场景依赖局部槽位布局，源码小改动就可能改变命中位置。
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Trim(
    /** 宿主方法描述符，格式为 `类#方法名(参数)返回`。 */
    val method: String,

    /** 改写目标类型：参数、返回值或局部变量。 */
    val kind: Kind,

    /** 参数索引（kind=ARG）或局部变量槽位（kind=VAR）；返回值场景通常保持默认。 */
    val index: Int = 0,

    /** 改写落点；默认在方法入口处理。 */
    val site: Site = Site(Anchor.HEAD),

    /** 额外的指令序列约束，用于在同一宿主方法中进一步缩小命中范围。 */
    val pattern: InsnPattern = InsnPattern([]),

    /** 命名标签，可供 DSL、诊断输出或后续匹配事件引用。 */
    val where: String = "",
) {

    /** Trim 支持的值类别。 */
    enum class Kind { ARG, RETURN, VAR }
}
