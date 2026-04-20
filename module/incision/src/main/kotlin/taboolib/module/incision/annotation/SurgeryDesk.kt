package taboolib.module.incision.annotation

/**
 * DSL 手术工作台声明。
 *
 * 用途：
 * 标记一个 Kotlin `object` 允许持有 `scalpel { ... }` 委托属性和 `scalpel.transient { ... }`
 * 短期手术块。
 *
 * 使用：
 * 把它标在调用 DSL 的 object 上。运行时会基于调用栈检查“当前是否位于合法 desk 内”。
 *
 * 效果：
 * 合法 desk 内声明的 DSL 手术会继承该 desk 的默认优先级，并通过栈帧校验获得运行资格。
 *
 * 局限：
 * 1. 如果在未标注的 object/class 内调用 DSL，会在 `provideDelegate` 或运行时检查阶段失败。
 * 2. 它只约束 DSL 调用来源，不影响注解式 `@Surgeon` 的扫描逻辑。
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class SurgeryDesk(

    /** 该 desk 内 DSL 手术的默认优先级；数值越大越先执行。 */
    val priority: Int = 0,
)
