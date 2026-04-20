package taboolib.module.incision.annotation

/**
 * 目标选择器注解。
 *
 * 用途：
 * 用一段 DSL 描述“这个 advice 到底针对哪些类、方法或字段”，避免把选择逻辑散落在代码里。
 *
 * 使用：
 * 可以标在 `@Surgeon` class 或具体 advice 方法上。
 * 常见语法：
 * - `class:com.foo.Bar`
 * - `method:Foo#bar(*)`
 * - `field:Foo#name:String`
 * - `&` 与，`|` 或，`!` 非
 *
 * 效果：
 * 运行时会先解析 [value]，再用它筛掉不相关目标，从而减少无意义织入。
 *
 * 局限：
 * 1. 它负责“挑对象”，不负责“挑字节码里的具体位置”；后者应交给 [Site] 或 [InsnPattern]。
 * 2. 复杂表达式越多，可读性越差，维护时应优先保持 selector 简洁。
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Scope(
    /** 选择器 DSL 原文。 */
    val value: String,
)
