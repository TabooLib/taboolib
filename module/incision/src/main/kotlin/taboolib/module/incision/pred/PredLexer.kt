package taboolib.module.incision.pred

import taboolib.module.incision.diagnostic.Trauma

/**
 * 谓词 DSL 词法分析器。
 *
 * 关键规则：
 * - `!` 单独 token（PredParser 在语义层组合 `!is` / `!ic` / `!ip` / `!it`，不在词法层预合成）。
 * - 类型算子 `as / is / ic / ip / it / matches / in` 走 Ident 路径，由 Parser 按位置识别。
 * - 数字字面量支持整数与小数；字符串用单/双引号；布尔 / null 走关键字 ident。
 * - 标识符：[A-Za-z_$][A-Za-z0-9_$]*，类型名（type_name）允许 `.`，由 Parser 在 type_op 后串联读取。
 */
internal class PredLexer(private val source: String) {

    enum class Kind {
        // 字面量
        Number, String, True, False, Null,
        // 标识符
        Ident,
        // 运算符 / 标点
        OrOr, AndAnd, Bang,
        Eq, Neq, Lt, Gt, Le, Ge,
        Dot, SafeDot, Comma,
        LParen, RParen, LBracket, RBracket,
        Eof
    }

    data class Token(val kind: Kind, val text: String, val pos: Int)

    private var pos = 0
    private val len = source.length

    fun tokenize(): List<Token> {
        val out = ArrayList<Token>(16)
        while (true) {
            val tk = nextToken() ?: continue
            out += tk
            if (tk.kind == Kind.Eof) return out
        }
    }

    private fun nextToken(): Token? {
        skipSpaces()
        if (pos >= len) return Token(Kind.Eof, "", pos)
        val start = pos
        val c = source[pos]
        return when {
            c.isDigit() -> readNumber(start)
            c == '"' || c == '\'' -> readString(start, c)
            isIdentStart(c) -> readIdent(start)
            else -> readSymbol(start, c)
        }
    }

    private fun skipSpaces() {
        while (pos < len) {
            val c = source[pos]
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') pos++ else break
        }
    }

    private fun readNumber(start: Int): Token {
        while (pos < len && source[pos].isDigit()) pos++
        if (pos < len && source[pos] == '.' && pos + 1 < len && source[pos + 1].isDigit()) {
            pos++
            while (pos < len && source[pos].isDigit()) pos++
        }
        return Token(Kind.Number, source.substring(start, pos), start)
    }

    private fun readString(start: Int, quote: Char): Token {
        pos++ // skip opening quote
        val sb = StringBuilder()
        while (pos < len) {
            val c = source[pos]
            if (c == '\\' && pos + 1 < len) {
                val nx = source[pos + 1]
                sb.append(
                    when (nx) {
                        'n' -> '\n'; 't' -> '\t'; 'r' -> '\r'
                        '\\' -> '\\'; '\'' -> '\''; '"' -> '"'
                        else -> nx
                    }
                )
                pos += 2
                continue
            }
            if (c == quote) {
                pos++
                return Token(Kind.String, sb.toString(), start)
            }
            sb.append(c); pos++
        }
        throw Trauma.Predicate.SyntaxError(source, start, "字符串字面量未闭合")
    }

    private fun readIdent(start: Int): Token {
        while (pos < len && isIdentPart(source[pos])) pos++
        val text = source.substring(start, pos)
        val kind = when (text) {
            "true" -> Kind.True
            "false" -> Kind.False
            "null" -> Kind.Null
            else -> Kind.Ident
        }
        return Token(kind, text, start)
    }

    private fun readSymbol(start: Int, c: Char): Token {
        return when (c) {
            '|' -> {
                if (peek(1) == '|') { pos += 2; Token(Kind.OrOr, "||", start) }
                else throw Trauma.Predicate.SyntaxError(source, start, "未识别字符 '|'，是否想写 '||'？")
            }
            '&' -> {
                if (peek(1) == '&') { pos += 2; Token(Kind.AndAnd, "&&", start) }
                else throw Trauma.Predicate.SyntaxError(source, start, "未识别字符 '&'，是否想写 '&&'？")
            }
            '!' -> {
                if (peek(1) == '=') { pos += 2; Token(Kind.Neq, "!=", start) }
                else { pos++; Token(Kind.Bang, "!", start) }
            }
            '=' -> {
                if (peek(1) == '=') { pos += 2; Token(Kind.Eq, "==", start) }
                else throw Trauma.Predicate.SyntaxError(source, start, "未识别字符 '='，是否想写 '=='？")
            }
            '<' -> if (peek(1) == '=') { pos += 2; Token(Kind.Le, "<=", start) } else { pos++; Token(Kind.Lt, "<", start) }
            '>' -> if (peek(1) == '=') { pos += 2; Token(Kind.Ge, ">=", start) } else { pos++; Token(Kind.Gt, ">", start) }
            '?' -> {
                if (peek(1) == '.') { pos += 2; Token(Kind.SafeDot, "?.", start) }
                else throw Trauma.Predicate.SyntaxError(source, start, "未识别字符 '?'，是否想写 '?.'？")
            }
            '.' -> { pos++; Token(Kind.Dot, ".", start) }
            ',' -> { pos++; Token(Kind.Comma, ",", start) }
            '(' -> { pos++; Token(Kind.LParen, "(", start) }
            ')' -> { pos++; Token(Kind.RParen, ")", start) }
            '[' -> { pos++; Token(Kind.LBracket, "[", start) }
            ']' -> { pos++; Token(Kind.RBracket, "]", start) }
            else -> throw Trauma.Predicate.SyntaxError(source, start, "未识别字符 '$c'")
        }
    }

    private fun peek(offset: Int): Char = if (pos + offset < len) source[pos + offset] else '\u0000'

    private fun isIdentStart(c: Char) = c.isLetter() || c == '_' || c == '$'
    private fun isIdentPart(c: Char) = c.isLetterOrDigit() || c == '_' || c == '$'
}
