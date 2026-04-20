package taboolib.module.incision.annotation

/**
 * 注解式施术者声明。
 *
 * 用途：
 * 把一个 Kotlin `object` 声明为 advice 持有者，让扫描器能在其中发现 `@Lead`、`@Trail`
 * 等注解方法。
 *
 * 使用：
 * 只能标在 Kotlin `object` 上；标在普通 class 上会在启动检查阶段抛出
 * `Trauma.Declaration.InvalidHolder`。
 *
 * 效果：
 * 被标注的 object 会参与扫描，其内部 advice 方法会按照类级默认优先级注册。
 *
 * 局限：
 * 1. 该注解只负责“这个 object 要不要被扫描”，不负责具体 target 选择。
 * 2. 类级 [priority] 只是默认值，仍可能被方法级 [Operation] 覆盖。
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Surgeon(

    /** 该 object 内 advice 的默认优先级；数值越大越先执行。 */
    val priority: Int = 0,
)
