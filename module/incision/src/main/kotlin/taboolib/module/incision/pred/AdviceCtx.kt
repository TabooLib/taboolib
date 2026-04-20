package taboolib.module.incision.pred

/**
 * 谓词编译上下文。由 advice 注册方提供，传给 [PredCompiler.compile]。
 *
 * @property adviceId    advice id，仅用于错误诊断（出现在 [taboolib.module.incision.diagnostic.Trauma.Predicate.RuntimeFailure] 中）。
 * @property classLoader 生成的谓词类的装载目标 ClassLoader。通常是声明 advice 的插件主 CL；
 *                       后续 script / external 场景可指向脚本沙箱 CL。
 * @property extraVars   除默认 `args/this/result/env/site/caller` 外允许出现的顶层变量名。
 *                       未列入白名单的变量会在编译期抛 [taboolib.module.incision.diagnostic.Trauma.Predicate.UndefinedVariable]。
 */
data class AdviceCtx(
    val adviceId: String,
    val classLoader: ClassLoader,
    val extraVars: Set<String> = emptySet(),
)
