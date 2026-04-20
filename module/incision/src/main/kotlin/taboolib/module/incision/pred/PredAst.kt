package taboolib.module.incision.pred

/**
 * 谓词 DSL 抽象语法树。
 *
 * 类型算子语义（语义层一元算子 `!` 与 `is/ic/ip/it` 在 [TypeCheck.negate] 中组合）：
 * - `is T`  → `T.isInstance(x)`
 * - `ic T`  → INSTANCEOF + 不等 class（即 isInstance 且 x.class != T）
 * - `ip T`  → `T.isAssignableFrom(x.class)`
 * - `it T`  → `x.class == T`
 * - `as T`  → 转换失败时谓词整体取 false（不抛）
 */
internal sealed class PredAst {

    /** `a || b || c` */
    data class Or(val items: List<PredAst>) : PredAst()

    /** `a && b && c` */
    data class And(val items: List<PredAst>) : PredAst()

    /** `!expr` 语义层一元 */
    data class Not(val target: PredAst) : PredAst()

    /** 二元比较，op∈ {==,!=,<,>,<=,>=,matches,in} */
    data class Cmp(val op: String, val left: PredAst, val right: PredAst) : PredAst()

    /**
     * 类型检查算子。
     * - kind ∈ {IS, IC, IP, IT}
     * - [negate] 表示是否带前缀 `!`（由 Parser 在 type_op 路径或 unary 路径合成）
     */
    data class TypeCheck(val target: PredAst, val kind: Kind, val typeName: String, val negate: Boolean) : PredAst() {
        enum class Kind { IS, IC, IP, IT }
    }

    /** `expr as T`：失败语义 → 整体谓词为 false（运行期） */
    data class As(val target: PredAst, val typeName: String) : PredAst()

    /** `.ident` 属性访问 */
    data class PropertyAccess(val receiver: PredAst, val name: String) : PredAst()

    /** `.ident(args)` 方法调用 */
    data class MethodCall(val receiver: PredAst, val name: String, val args: List<PredAst>) : PredAst()

    /** `[expr]` 下标访问 */
    data class Index(val receiver: PredAst, val index: PredAst) : PredAst()

    /** `?.` 安全访问标记，作用于其子节点（PropertyAccess/MethodCall），缺省语义：null 短路返回 false。 */
    data class SafeAccess(val target: PredAst) : PredAst()

    /** 顶层标识符引用，例如 `args` / `result` / `this` / `env` */
    data class Var(val name: String) : PredAst()

    /** 字面量：Int / Long / Double / String / Boolean / null */
    data class Literal(val value: Any?) : PredAst()

    /** `( expr )` —— 保留括号节点便于诊断输出，编译期等价于内部表达式 */
    data class Paren(val inner: PredAst) : PredAst()
}
