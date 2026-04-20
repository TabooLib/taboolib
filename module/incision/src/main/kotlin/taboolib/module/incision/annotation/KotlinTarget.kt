package taboolib.module.incision.annotation

/**
 * Kotlin 特殊目标扩展声明。
 *
 * 用途：
 * 当目标来自 Kotlin companion 或 `@JvmStatic` 桥接方法时，显式声明同一 advice
 * 是否还要附加到这些额外生成的方法上。
 *
 * 使用：
 * 把它加在 advice 方法上，并根据需要开启 [companionInstance] 或 [jvmStaticBridge]。
 *
 * 效果：
 * 扫描阶段会在主 target 之外，再补充 companion 实例方法或静态桥接方法对应的 target。
 *
 * 局限：
 * 1. 依赖 Kotlin 编译器生成代码的具体形态，升级编译器后应回归测试。
 * 2. 只解决 Kotlin 额外方法映射问题，不负责 remap 或版本过滤。
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class KotlinTarget(
    /** 是否额外挂到 companion 实例方法上。 */
    val companionInstance: Boolean = false,
    /** 是否额外挂到 `@JvmStatic` 生成的静态桥接方法上。 */
    val jvmStaticBridge: Boolean = false,
)
