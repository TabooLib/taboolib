package taboolib.module.incision.scope

import taboolib.module.incision.diagnostic.Trauma

/**
 * Scope DSL 语法树。
 *
 * 语法：
 * - `class:com.foo.Bar`         匹配类（支持 `*` 通配）
 * - `method:Foo#bar(*)`         匹配方法（`*` 通配描述符段）
 * - `field:Foo#name:String`     匹配字段
 * - `&` 与, `|` 或, `!` 非, `(...)` 分组
 */
sealed class ScopeNode {
    abstract fun matches(target: ScopeTarget): Boolean

    class And(val left: ScopeNode, val right: ScopeNode) : ScopeNode() {
        override fun matches(t: ScopeTarget) = left.matches(t) && right.matches(t)
    }
    class Or(val left: ScopeNode, val right: ScopeNode) : ScopeNode() {
        override fun matches(t: ScopeTarget) = left.matches(t) || right.matches(t)
    }
    class Not(val inner: ScopeNode) : ScopeNode() {
        override fun matches(t: ScopeTarget) = !inner.matches(t)
    }
    class ClassMatch(val pattern: String) : ScopeNode() {
        private val regex = patternToRegex(pattern)
        override fun matches(t: ScopeTarget) = regex.matches(t.className)
    }
    class MethodMatch(val owner: String, val name: String, val descPattern: String) : ScopeNode() {
        private val ownerRegex = patternToRegex(owner)
        private val nameRegex = patternToRegex(name)
        private val descRegex = patternToRegex(descPattern)
        override fun matches(t: ScopeTarget) =
            t.kind == ScopeTarget.Kind.METHOD &&
            ownerRegex.matches(t.className) &&
            nameRegex.matches(t.memberName ?: "") &&
            descRegex.matches(t.descriptor ?: "")
    }
    class FieldMatch(val owner: String, val name: String, val typePattern: String) : ScopeNode() {
        private val ownerRegex = patternToRegex(owner)
        private val nameRegex = patternToRegex(name)
        private val typeRegex = patternToRegex(typePattern)
        override fun matches(t: ScopeTarget) =
            t.kind == ScopeTarget.Kind.FIELD &&
            ownerRegex.matches(t.className) &&
            nameRegex.matches(t.memberName ?: "") &&
            typeRegex.matches(t.descriptor ?: "")
    }

    companion object {
        internal fun patternToRegex(p: String): Regex {
            val sb = StringBuilder()
            for (ch in p) when (ch) {
                '*' -> sb.append(".*")
                '?' -> sb.append('.')
                '.', '$', '(', ')', '[', ']', '+', '^' -> sb.append('\\').append(ch)
                else -> sb.append(ch)
            }
            return Regex(sb.toString())
        }
    }
}

/** ScopeNode.matches 的输入 — 描述某个候选目标 */
data class ScopeTarget(
    val kind: Kind,
    val className: String,
    val memberName: String? = null,
    val descriptor: String? = null,
) {
    enum class Kind { CLASS, METHOD, FIELD }
}

/**
 * 词法 + 语法分析。
 *
 * 出错时抛 [Trauma.Declaration.BadScope]，附带原文与定位。
 */
object ScopeParser {

    fun parse(raw: String): ScopeNode {
        val tokens = tokenize(raw)
        val parser = Parser(raw, tokens)
        val node = parser.parseExpr()
        if (parser.cursor < tokens.size) {
            throw Trauma.Declaration.BadScope(raw, parser.posOf(parser.cursor), "意外的 token: ${tokens[parser.cursor]}")
        }
        return node
    }

    private sealed class Tok(val pos: Int) {
        class Atom(pos: Int, val text: String) : Tok(pos)
        class And(pos: Int) : Tok(pos)
        class Or(pos: Int) : Tok(pos)
        class Not(pos: Int) : Tok(pos)
        class LParen(pos: Int) : Tok(pos)
        class RParen(pos: Int) : Tok(pos)
    }

    private fun tokenize(raw: String): List<Tok> {
        val out = mutableListOf<Tok>()
        var i = 0
        while (i < raw.length) {
            val c = raw[i]
            when {
                c.isWhitespace() -> { i++ }
                c == '&' -> { out += Tok.And(i); i++ }
                c == '|' -> { out += Tok.Or(i); i++ }
                c == '!' -> { out += Tok.Not(i); i++ }
                c == '(' -> { out += Tok.LParen(i); i++ }
                c == ')' -> { out += Tok.RParen(i); i++ }
                else -> {
                    val start = i
                    while (i < raw.length && raw[i] !in "&|!() \t") i++
                    out += Tok.Atom(start, raw.substring(start, i))
                }
            }
        }
        return out
    }

    private class Parser(val raw: String, val tokens: List<Tok>) {
        var cursor = 0
        fun posOf(idx: Int) = if (idx < tokens.size) tokens[idx].pos else raw.length

        fun parseExpr(): ScopeNode = parseOr()

        fun parseOr(): ScopeNode {
            var left = parseAnd()
            while (cursor < tokens.size && tokens[cursor] is Tok.Or) {
                cursor++
                left = ScopeNode.Or(left, parseAnd())
            }
            return left
        }
        fun parseAnd(): ScopeNode {
            var left = parseNot()
            while (cursor < tokens.size && tokens[cursor] is Tok.And) {
                cursor++
                left = ScopeNode.And(left, parseNot())
            }
            return left
        }
        fun parseNot(): ScopeNode {
            if (cursor < tokens.size && tokens[cursor] is Tok.Not) {
                cursor++
                return ScopeNode.Not(parseAtom())
            }
            return parseAtom()
        }
        fun parseAtom(): ScopeNode {
            if (cursor >= tokens.size) {
                throw Trauma.Declaration.BadScope(raw, raw.length, "意外结尾")
            }
            return when (val t = tokens[cursor]) {
                is Tok.LParen -> {
                    cursor++
                    val n = parseExpr()
                    if (cursor >= tokens.size || tokens[cursor] !is Tok.RParen) {
                        throw Trauma.Declaration.BadScope(raw, posOf(cursor), "缺少 ')'")
                    }
                    cursor++
                    n
                }
                is Tok.Atom -> {
                    cursor++
                    parseAtomText(t.text, t.pos)
                }
                else -> throw Trauma.Declaration.BadScope(raw, t.pos, "意外 token: $t")
            }
        }
        fun parseAtomText(text: String, pos: Int): ScopeNode {
            val colon = text.indexOf(':')
            if (colon < 0) throw Trauma.Declaration.BadScope(raw, pos, "缺少 'class:' / 'method:' / 'field:' 前缀: '$text'")
            val kind = text.substring(0, colon)
            val body = text.substring(colon + 1)
            return when (kind) {
                "class" -> ScopeNode.ClassMatch(body)
                "method" -> {
                    val hash = body.indexOf('#')
                    if (hash < 0) throw Trauma.Declaration.BadScope(raw, pos, "method 缺少 '#': '$body'")
                    val owner = body.substring(0, hash)
                    val rest = body.substring(hash + 1)
                    val paren = rest.indexOf('(')
                    val name = if (paren < 0) rest else rest.substring(0, paren)
                    val desc = if (paren < 0) "*" else rest.substring(paren)
                    ScopeNode.MethodMatch(owner, name, desc)
                }
                "field" -> {
                    val hash = body.indexOf('#')
                    if (hash < 0) throw Trauma.Declaration.BadScope(raw, pos, "field 缺少 '#': '$body'")
                    val owner = body.substring(0, hash)
                    val rest = body.substring(hash + 1)
                    val typeIdx = rest.indexOf(':')
                    val name = if (typeIdx < 0) rest else rest.substring(0, typeIdx)
                    val type = if (typeIdx < 0) "*" else rest.substring(typeIdx + 1)
                    ScopeNode.FieldMatch(owner, name, type)
                }
                else -> throw Trauma.Declaration.BadScope(raw, pos, "未知前缀 '$kind'，期望 class/method/field")
            }
        }
    }
}
