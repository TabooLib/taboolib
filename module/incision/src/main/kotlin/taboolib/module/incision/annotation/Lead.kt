package taboolib.module.incision.annotation

/**
 * 先导 advice。
 *
 * 用途：
 * 在目标方法真正进入方法体之前插入一段只读或轻量改写逻辑，等价于
 * Spring `@Before` 或 Mixin `@Inject(at = @At("HEAD"))` 的入口切入。
 *
 * 使用：
 * 把注解标在 `@Surgeon` object 内的方法上，并通过 [scope] 指定要命中的目标。
 * handler 常见签名为 `fun(theatre: Theatre)`，通过 theatre 读取 self、args、上下文信息。
 *
 * 效果：
 * 命中后会在方法入口处额外触发一次 advice，不会替换原方法，也不会自动中断原流程。
 * 如需控制是否继续执行原方法，应改用 [Splice]。
 *
 * 局限：
 * 1. 该注解主要表达“入口发生了什么”，不擅长替换中段调用点。
 * 2. 如果同时配合 [pattern]，最终是否命中仍取决于编译后的真实字节码形态。
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Lead(

    /** 目标选择器 DSL，例如 `method:org.bukkit.entity.Player#kickPlayer(*)`。 */
    val scope: String,

    /** 额外的指令序列约束，用于把入口切入缩小到满足特定字节码形态的目标方法。 */
    val pattern: InsnPattern = InsnPattern([]),

    /** 命名标签，可供 DSL、诊断输出或后续匹配事件引用。 */
    val where: String = "",
)
