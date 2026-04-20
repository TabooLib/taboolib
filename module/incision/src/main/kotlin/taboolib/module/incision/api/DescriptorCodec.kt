package taboolib.module.incision.api

/**
 * 描述符工具 — 把用户写的宽松描述符规范为 JVM 描述符。
 *
 * 支持：
 * - 完整 `类#方法名(Ljava/lang/String;)V`
 * - 通配实参 `类#方法名(*)` → 后期 scope 匹配
 * - `{version}` 占位符（需与 NMS resolver 协同）
 * - 短描述符 `String, int` → `Ljava/lang/String;I`
 */
object DescriptorCodec {

    private val primitives = mapOf(
        "void" to "V", "boolean" to "Z", "byte" to "B", "short" to "S",
        "int" to "I", "long" to "J", "float" to "F", "double" to "D", "char" to "C",
    )

    /**
     * 把 `类#方法名(参数)返回` 拆成 owner / name / 方法描述符。
     * 返回 null 表示解析失败（不抛异常，交给上游决定处理方式）。
     */
    fun parseMethod(raw: String): Parsed? {
        val hash = raw.indexOf('#')
        if (hash <= 0 || hash == raw.length - 1) return null
        val owner = raw.substring(0, hash).trim().takeIf { it.isNotEmpty() }?.replace('.', '/') ?: return null
        val rest = raw.substring(hash + 1).trim()
        if (rest.isEmpty()) return null
        val paren = rest.indexOf('(')
        if (paren <= 0) return null
        val close = rest.indexOf(')', paren)
        if (close < 0) return null
        val name = rest.substring(0, paren)
        if (name.isBlank() || name.any(Char::isWhitespace)) return null
        val argsPart = rest.substring(paren + 1, close)
        val retPart = rest.substring(close + 1).ifBlank { if (argsPart == "*") "*" else "V" }
        val argsDesc = encodeArgs(argsPart)
        val retDesc = encodeType(retPart)
        return Parsed(owner, name, "($argsDesc)$retDesc")
    }

    fun parseField(raw: String): ParsedField? {
        val hash = raw.indexOf('#')
        if (hash < 0) return null
        val owner = raw.substring(0, hash).replace('.', '/')
        val rest = raw.substring(hash + 1)
        val colon = rest.indexOf(':')
        if (colon < 0) return null
        val name = rest.substring(0, colon)
        val type = rest.substring(colon + 1)
        return ParsedField(owner, name, encodeType(type))
    }

    private fun encodeArgs(args: String): String {
        if (args.isBlank() || args == "*") return args
        return args.split(',').filter { it.isNotBlank() }.joinToString("") { encodeType(it.trim()) }
    }

    private val jvmPrimitiveDescriptors = setOf("V", "Z", "B", "S", "I", "J", "F", "D", "C")

    fun encodeType(raw: String): String {
        if (raw == "*") return "*"
        var arr = 0
        var t = raw
        while (t.endsWith("[]")) { arr++; t = t.dropLast(2) }
        val base = primitives[t] ?: when {
            t.length == 1 && t in jvmPrimitiveDescriptors -> t
            t.startsWith("L") && t.endsWith(";") -> t
            else -> "L${t.replace('.', '/')};"
        }
        return "[".repeat(arr) + base
    }

    data class Parsed(val owner: String, val name: String, val descriptor: String) {
        fun toCoordinate() = MethodCoordinate(owner, name, descriptor)
    }
    data class ParsedField(val owner: String, val name: String, val descriptor: String)
}
