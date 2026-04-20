package taboolib.module.incision.annotation

/**
 * 指令序列模式。
 *
 * 用途：
 * 用一组 [Step] 描述“目标方法里必须出现怎样的字节码片段”，从而把匹配从方法级进一步收紧到
 * 特定字节码形态。
 *
 * 使用：
 * 把它填到各类 advice 的 `pattern` 参数中；如果还想在 DSL 或诊断里引用该模式，
 * 可以搭配 `where` 给它起一个稳定名字。
 *
 * 效果：
 * 运行时会按顺序匹配 [steps]，只有整段序列都满足时，目标方法才会被视为命中。
 *
 * 局限：
 * 1. 它面向编译后字节码，不是面向源码；编译器优化、常量折叠都会影响结果。
 * 2. 空 `steps` 表示关闭该约束，而不是“匹配空序列”。
 */
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class InsnPattern(
    /** 顺序匹配的步骤数组；留空表示不启用模式约束。 */
    val steps: Array<Step> = [],
)
