package taboolib.module.incision.pred

import taboolib.module.incision.diagnostic.Trauma
import taboolib.module.incision.pred.PredLexer.Kind
import taboolib.module.incision.pred.PredLexer.Token

/**
 * 谓词 DSL 递归下降语法分析器。
 *
 * 文法（已锁定，见 #9）：
 * ```
 * expr     := or
 * or       := and ('||' and)*
 * and      := type_cmp ('&&' type_cmp)*
 * type_cmp := cmp (type_op type_name)?
 * type_op  := 'as' | 'is' | '!is' | 'ic' | '!ic' | 'ip' | '!ip' | 'it' | '!it'
 * cmp      := unary (('==' | '!=' | '<' | '>' | '<=' | '>=' | 'matches' | 'in') unary)?
 * unary    := '!' unary | postfix
 * postfix  := primary access*
 * access   := '.' ident
 *           | '.' ident '(' args? ')'
 *           | '[' expr ']'
 *           | '?.' (ident | ident '(' args? ')')
 * primary  := literal | ident | '(' expr ')'
 * ```
 *
 * 关键约束：
 * - `args.size[0]` 等"方法结果再做下标"的形式，由 [accessLoop] 在产出 [PredAst.MethodCall] 后立即看见 `[` 时
 *   抛 `Trauma.Predicate.MethodIndexed`。
 * - `!is/!ic/!ip/!it` 词法层不预合成；解析 type_op 时若上一 token 是 `!` 则携带 negate=true。
 */
internal class PredParser(private val source: String) {

    private val tokens: List<Token> = PredLexer(source).tokenize()
    private var idx = 0

    fun parse(): PredAst {
        val ast = parseOr()
        expect(Kind.Eof, "表达式末尾存在多余内容")
        return ast
    }

    // ---------- or / and ----------

    private fun parseOr(): PredAst {
        val first = parseAnd()
        if (peek().kind != Kind.OrOr) return first
        val items = ArrayList<PredAst>(2).apply { add(first) }
        while (peek().kind == Kind.OrOr) { advance(); items += parseAnd() }
        return PredAst.Or(items)
    }

    private fun parseAnd(): PredAst {
        val first = parseTypeCmp()
        if (peek().kind != Kind.AndAnd) return first
        val items = ArrayList<PredAst>(2).apply { add(first) }
        while (peek().kind == Kind.AndAnd) { advance(); items += parseTypeCmp() }
        return PredAst.And(items)
    }

    // ---------- type_cmp ----------

    private fun parseTypeCmp(): PredAst {
        val left = parseCmp()
        // 此处的 `!is` 等通过先看 `!` 再看 ident 实现
        val (op, negate, opPos) = peekTypeOp() ?: return left
        // 消耗 token：可能是 1 个（is/ic/ip/it/as）或 2 个（! + is/ic/ip/it）
        if (negate) advance() // !
        advance() // type_op ident
        val typeName = readTypeName(opPos)
        return when (op) {
            "as" -> {
                if (negate) throw Trauma.Predicate.SyntaxError(source, opPos, "'as' 不支持 '!as' 形式")
                PredAst.As(left, typeName)
            }
            "is" -> PredAst.TypeCheck(left, PredAst.TypeCheck.Kind.IS, typeName, negate)
            "ic" -> PredAst.TypeCheck(left, PredAst.TypeCheck.Kind.IC, typeName, negate)
            "ip" -> PredAst.TypeCheck(left, PredAst.TypeCheck.Kind.IP, typeName, negate)
            "it" -> PredAst.TypeCheck(left, PredAst.TypeCheck.Kind.IT, typeName, negate)
            else -> error("unreachable")
        }
    }

    /** 返回 (op, negate, opPos)；不消耗 token。 */
    private fun peekTypeOp(): Triple<String, Boolean, Int>? {
        val t0 = peek()
        if (t0.kind == Kind.Bang) {
            val t1 = peek(1)
            if (t1.kind == Kind.Ident && t1.text in TYPE_OPS_NEGATABLE) {
                return Triple(t1.text, true, t1.pos)
            }
            return null
        }
        if (t0.kind == Kind.Ident && t0.text in TYPE_OPS_ALL) {
            return Triple(t0.text, false, t0.pos)
        }
        return null
    }

    private fun readTypeName(opPos: Int): String {
        val first = peek()
        if (first.kind != Kind.Ident) {
            throw Trauma.Predicate.SyntaxError(source, first.pos, "类型算子之后期望类型名")
        }
        val sb = StringBuilder(first.text); advance()
        while (peek().kind == Kind.Dot && peek(1).kind == Kind.Ident) {
            advance(); sb.append('.').append(peek().text); advance()
        }
        if (sb.isEmpty()) throw Trauma.Predicate.SyntaxError(source, opPos, "类型名为空")
        return sb.toString()
    }

    // ---------- cmp ----------

    private fun parseCmp(): PredAst {
        val left = parseUnary()
        val t = peek()
        val op = when (t.kind) {
            Kind.Eq -> "=="; Kind.Neq -> "!="
            Kind.Lt -> "<"; Kind.Gt -> ">"; Kind.Le -> "<="; Kind.Ge -> ">="
            Kind.Ident -> when (t.text) { "matches" -> "matches"; "in" -> "in"; else -> null }
            else -> null
        } ?: return left
        advance()
        val right = parseUnary()
        return PredAst.Cmp(op, left, right)
    }

    // ---------- unary ----------

    private fun parseUnary(): PredAst {
        if (peek().kind == Kind.Bang) {
            // 注意：若是 `!is/!ic/!ip/!it` 的形式，由 parseTypeCmp 在更外层消化；
            // 此处只处理"作用于表达式"的 `!`。需要前看一位避免吞掉 `! is`。
            val n1 = peek(1)
            if (!(n1.kind == Kind.Ident && n1.text in TYPE_OPS_NEGATABLE)) {
                advance()
                return PredAst.Not(parseUnary())
            }
        }
        return parsePostfix()
    }

    // ---------- postfix / access ----------

    private fun parsePostfix(): PredAst {
        var node = parsePrimary()
        node = accessLoop(node)
        return node
    }

    private fun accessLoop(start: PredAst): PredAst {
        var node = start
        while (true) {
            val t = peek()
            when (t.kind) {
                Kind.Dot -> {
                    advance()
                    val nameTk = expectIdent("'.' 之后期望成员名")
                    node = if (peek().kind == Kind.LParen) {
                        val args = parseCallArgs()
                        val call = PredAst.MethodCall(node, nameTk.text, args)
                        if (peek().kind == Kind.LBracket) {
                            throw Trauma.Predicate.MethodIndexed(source, nameTk.text)
                        }
                        call
                    } else {
                        val prop = PredAst.PropertyAccess(node, nameTk.text)
                        if (peek().kind == Kind.LBracket) {
                            throw Trauma.Predicate.MethodIndexed(source, nameTk.text)
                        }
                        prop
                    }
                }
                Kind.SafeDot -> {
                    advance()
                    val nameTk = expectIdent("'?.' 之后期望成员名")
                    val inner = if (peek().kind == Kind.LParen) {
                        val args = parseCallArgs()
                        val call = PredAst.MethodCall(node, nameTk.text, args)
                        if (peek().kind == Kind.LBracket) {
                            throw Trauma.Predicate.MethodIndexed(source, nameTk.text)
                        }
                        call
                    } else {
                        val prop = PredAst.PropertyAccess(node, nameTk.text)
                        if (peek().kind == Kind.LBracket) {
                            throw Trauma.Predicate.MethodIndexed(source, nameTk.text)
                        }
                        prop
                    }
                    node = PredAst.SafeAccess(inner)
                }
                Kind.LBracket -> {
                    advance()
                    val idxExpr = parseOr()
                    expect(Kind.RBracket, "下标缺少 ']'")
                    node = PredAst.Index(node, idxExpr)
                }
                else -> return node
            }
        }
    }

    private fun parseCallArgs(): List<PredAst> {
        expect(Kind.LParen, "期望 '('")
        if (peek().kind == Kind.RParen) { advance(); return emptyList() }
        val args = ArrayList<PredAst>(2)
        args += parseOr()
        while (peek().kind == Kind.Comma) { advance(); args += parseOr() }
        expect(Kind.RParen, "参数列表缺少 ')'")
        return args
    }

    // ---------- primary ----------

    private fun parsePrimary(): PredAst {
        val t = peek()
        return when (t.kind) {
            Kind.Number -> {
                advance()
                val v: Any = if (t.text.contains('.')) t.text.toDouble() else {
                    val l = t.text.toLong()
                    if (l in Int.MIN_VALUE..Int.MAX_VALUE) l.toInt() else l
                }
                PredAst.Literal(v)
            }
            Kind.String -> { advance(); PredAst.Literal(t.text) }
            Kind.True -> { advance(); PredAst.Literal(true) }
            Kind.False -> { advance(); PredAst.Literal(false) }
            Kind.Null -> { advance(); PredAst.Literal(null) }
            Kind.Ident -> { advance(); PredAst.Var(t.text) }
            Kind.LParen -> {
                advance()
                val e = parseOr()
                expect(Kind.RParen, "缺少 ')'")
                PredAst.Paren(e)
            }
            else -> throw Trauma.Predicate.SyntaxError(source, t.pos, "意外的 token '${t.text}'")
        }
    }

    // ---------- helpers ----------

    private fun peek(offset: Int = 0): Token = tokens[(idx + offset).coerceAtMost(tokens.size - 1)]
    private fun advance(): Token = tokens[idx++]
    private fun expect(kind: Kind, reason: String): Token {
        val t = peek()
        if (t.kind != kind) throw Trauma.Predicate.SyntaxError(source, t.pos, "$reason，实际: '${t.text}'")
        return advance()
    }
    private fun expectIdent(reason: String): Token {
        val t = peek()
        if (t.kind != Kind.Ident) throw Trauma.Predicate.SyntaxError(source, t.pos, "$reason，实际: '${t.text}'")
        return advance()
    }

    companion object {
        private val TYPE_OPS_NEGATABLE = setOf("is", "ic", "ip", "it")
        private val TYPE_OPS_ALL = setOf("as", "is", "ic", "ip", "it")

        @JvmStatic
        fun main(args: Array<String>) {
            val cases = listOf(
                "args[0] is java.lang.String && result != null",
                "this.name == 'Steve' || env.size() > 0",
                "!(args[0] ic Number) && args.size() == 2",
                "(result as java.util.List)?.isEmpty() == false",
                "caller().name matches 'Player.*'"
            )
            for (src in cases) {
                println("== $src")
                try {
                    println(PredParser(src).parse())
                } catch (e: Trauma.Predicate) {
                    println("  ! ${e.message}")
                }
            }
        }
    }
}
