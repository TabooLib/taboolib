package taboolib.module.incision.pred

/**
 * 谓词求值上下文。
 *
 * 由 [taboolib.module.incision.runtime.TheatreDispatcher] 在每次调用 advice 前构造，
 * 用于把 `args / this / result / env / site / caller` 等 DSL 顶层变量按需暴露给已编译的 [Predicate].test。
 *
 * 设计原则（懒求值）：
 * - 大多数 advice 的谓词不会真正使用 `result()`（只在 TRAIL 阶段有意义）或 `caller()`（昂贵的栈回溯），
 *   因此这些方法应在第一次调用时才计算 / 反射，结果缓存到下一次 reset。
 * - 由 dispatcher 持有一个池化的 EvalContext 实例，每条 advice 调用前 reset。
 */
interface EvalContext {

    /** 第 i 个原方法实参（含可空）。i 越界时抛 IndexOutOfBoundsException —— 由编译器在 `args.size()` 等场景规避。 */
    fun argAt(i: Int): Any?

    /** 原方法实参个数。 */
    fun argCount(): Int

    /** 原方法的 this（静态方法 → null）。 */
    fun thisRef(): Any?

    /** 原方法的返回值（仅 TRAIL 阶段有效；其他阶段 → null）。 */
    fun result(): Any?

    /** 用户在 advice 注册时附加的环境 map（只读视图）。 */
    fun env(): Map<String, Any?>

    /** 当前切术站点信息（owner / name / desc，可能为 null —— 解析失败或非站点谓词）。 */
    fun site(): Any?

    /** 调用方信息（栈回溯，按需懒计算）。 */
    fun caller(): Any?
}
