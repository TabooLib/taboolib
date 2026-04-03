package taboolib.common5

/**
 * Demand 是一个命令行风格的字符串解析工具，用于将格式化的字符串解析为结构化数据。
 *
 * 基本格式：
 * ```
 * namespace [args...] [-key value...] [--tags...]
 * ```
 *
 * 解析规则：
 * 1. 第一个参数被视为命名空间（namespace）
 * 2. 以 "-" 开头的参数被视为键（key），其后的参数被视为值（value）
 * 3. 以 "--" 开头的参数被视为标签（tag）
 * 4. 不带前缀的参数被视为普通参数（args）
 * 5. 支持使用双引号（"）或单引号（'）包裹包含空格的字符串参数
 * 6. 使用反斜杠（\）转义引号（\" 或 \'）
 *
 * 使用示例：
 * ```
 * val demand = Demand("command arg1 arg2 -key1 value1 -key2 \"value with spaces\" --tag1 --tag2")
 *
 * // 访问命名空间
 * val namespace = demand.namespace // "command"
 *
 * // 访问普通参数
 * val arg1 = demand.get(0) // "arg1"
 * val arg2 = demand.get(1) // "arg2"
 *
 * // 访问键值对
 * val key1 = demand.get("key1") // "value1"
 * val key2 = demand.get("key2") // "value with spaces"
 *
 * // 检查标签
 * val hasTag1 = "tag1" in demand.tags // true
 * ```
 *
 * @author bkm016
 * @since 2020/11/22 2:51 下午
 */
@Suppress("ReplaceSubstringWithDropLast", "ReplaceSubstringWithTake")
class Demand(source: String) {

    val source = source.trim()

    lateinit var namespace: String

    val dataMap = HashMap<String, String>()
    val args = ArrayList<String>()
    val tags = ArrayList<String>()

    /**
     * 解析状态枚举
     */
    private enum class ParseState {
        NORMAL,      // 普通状态
        IN_QUOTES,   // 在引号内
        AFTER_KEY    // 键之后，等待值
    }

    /** 当前引号字符（" 或 '） */
    private var currentQuote = '"'

    private fun isQuoteChar(ch: Char): Boolean = ch == '"' || ch == '\''

    init {
        parse()
    }

    private fun parse() {
        // 处理空字符串情况
        if (source.isEmpty()) {
            namespace = ""
            return
        }
        // 分割参数
        val splitArgs = source.split(' ')
        namespace = splitArgs.firstOrNull() ?: ""
        // 如果只有命名空间，直接返回
        if (splitArgs.size <= 1) {
            return
        }
        // 解析剩余参数
        parseArguments(splitArgs.subList(1, splitArgs.size))
    }

    /**
     * 解析参数列表
     *
     * @param argList 参数列表
     */
    private fun parseArguments(argList: List<String>) {
        var state = ParseState.NORMAL
        var currentKey: String? = null
        val buffer = ArrayList<String>()
        for (arg in argList) {
            when (state) {
                ParseState.NORMAL -> handleNormalState(arg, buffer, { key -> currentKey = key }, { state = it })
                ParseState.IN_QUOTES -> handleQuotedState(arg, buffer, currentKey, { key -> currentKey = key }, { state = it })
                ParseState.AFTER_KEY -> handleAfterKeyState(arg, buffer, currentKey, { key -> currentKey = key }, { state = it })
            }
        }
        // 处理未闭合的引号
        if (state == ParseState.IN_QUOTES && buffer.isNotEmpty()) {
            handleUnclosedQuotes(buffer, currentKey)
        }
        // 处理未赋值的键（转换为args）
        if (state == ParseState.AFTER_KEY && currentKey != null) {
            args.add("-$currentKey")
        }
    }

    /**
     * 处理普通状态下的参数
     */
    private fun handleNormalState(
        arg: String,
        buffer: ArrayList<String>,
        setKey: (String) -> Unit,
        setState: (ParseState) -> Unit
    ) {
        if (arg.isEmpty()) {
            return
        }
        val ch = arg[0]
        when {
            // 处理标签
            arg.length > 1 && ch == '-' && arg[1] == '-' -> {
                tags.add(arg.substring(2))
            }
            // 处理键
            ch == '-' -> {
                setKey(arg.substring(1))
                setState(ParseState.AFTER_KEY)
            }
            // 处理带引号的参数
            isQuoteChar(ch) -> {
                currentQuote = ch
                if (arg.endsWith(ch) && !arg.endsWith("\\$ch") && arg.length > 1) {
                    // 单个参数中的完整引号字符串
                    args.add(arg.substring(1, arg.length - 1).unescapeQuotes())
                } else {
                    // 开始多个参数的引号字符串
                    buffer.add(arg.substring(1))
                    setState(ParseState.IN_QUOTES)
                }
            }
            // 处理普通参数
            else -> {
                args.add(arg)
            }
        }
    }

    /**
     * 处理引号状态下的参数
     */
    private fun handleQuotedState(
        arg: String,
        buffer: ArrayList<String>,
        currentKey: String?,
        setKey: (String?) -> Unit,
        setState: (ParseState) -> Unit
    ) {
        if (arg.endsWith(currentQuote) && !arg.endsWith("\\$currentQuote")) {
            // 引号结束
            buffer.add(arg.substring(0, arg.length - 1))
            val value = joinBuffer(buffer).unescapeQuotes()
            if (currentKey != null) {
                dataMap[currentKey] = value
                setKey(null)
            } else {
                args.add(value)
            }
            setState(ParseState.NORMAL)
            buffer.clear()
        } else {
            // 继续在引号内
            buffer.add(arg)
        }
    }

    /**
     * 处理键之后的状态
     */
    private fun handleAfterKeyState(
        arg: String,
        buffer: ArrayList<String>,
        currentKey: String?,
        setKey: (String?) -> Unit,
        setState: (ParseState) -> Unit
    ) {
        if (currentKey == null) {
            // 异常情况，不应该发生
            setState(ParseState.NORMAL)
            return
        }
        when {
            // 处理带引号的值
            arg.isNotEmpty() && isQuoteChar(arg[0]) -> {
                currentQuote = arg[0]
                if (arg.endsWith(currentQuote) && !arg.endsWith("\\$currentQuote") && arg.length > 1) {
                    // 单个参数中的完整引号字符串
                    dataMap[currentKey] = arg.substring(1, arg.length - 1).unescapeQuotes()
                    setKey(null)
                    setState(ParseState.NORMAL)
                } else {
                    // 开始多个参数的引号字符串
                    buffer.add(arg.substring(1))
                    setState(ParseState.IN_QUOTES)
                }
            }
            // 处理新的标签或键（当前键没有值）
            arg.startsWith('-') -> {
                // 当前键没有值，将其作为普通参数
                args.add("-$currentKey")
                // 处理新的标签或键
                if (arg.startsWith("--")) {
                    tags.add(arg.substring(2))
                    setKey(null)
                    setState(ParseState.NORMAL)
                } else {
                    setKey(arg.substring(1))
                    setState(ParseState.AFTER_KEY)
                }
            }
            // 处理普通值
            else -> {
                dataMap[currentKey] = arg
                setKey(null)
                setState(ParseState.NORMAL)
            }
        }
    }

    /**
     * 处理未闭合的引号
     */
    private fun handleUnclosedQuotes(buffer: ArrayList<String>, currentKey: String?) {
        val value = joinBuffer(buffer).unescapeQuotes()
        if (currentKey != null) {
            dataMap[currentKey] = value
        } else {
            args.add(value)
        }
        buffer.clear()
    }

    /**
     * 优化的 buffer 拼接方法，使用 StringBuilder 替代 joinToString
     */
    private fun joinBuffer(buffer: ArrayList<String>): String {
        if (buffer.isEmpty()) return ""
        if (buffer.size == 1) return buffer[0]
        val sb = StringBuilder(buffer[0])
        for (i in 1 until buffer.size) {
            sb.append(' ').append(buffer[i])
        }
        return sb.toString()
    }

    /**
     * 去除字符串中的转义引号
     */
    private fun String.unescapeQuotes(): String {
        return this.replace("\\\"", "\"").replace("\\'", "'")
    }

    fun get(key: List<String>, def: String? = null): String? {
        return key.firstNotNullOfOrNull { get(it) } ?: def
    }

    fun get(key: String, def: String? = null): String? {
        return dataMap[key] ?: def
    }

    fun get(index: Int, def: String? = null): String? {
        return args.getOrNull(index) ?: def
    }

    /**
     * 获取整数值
     *
     * @param key 键名
     * @param def 默认值
     * @return 整数值，如果转换失败则返回默认值
     */
    fun getInt(key: String, def: Int = 0): Int {
        return get(key)?.cint ?: def
    }

    /**
     * 获取浮点数值
     *
     * @param key 键名
     * @param def 默认值
     * @return 浮点数值，如果转换失败则返回默认值
     */
    fun getDouble(key: String, def: Double = 0.0): Double {
        return get(key)?.cdouble ?: def
    }

    /**
     * 获取布尔值
     *
     * @param key 键名
     * @param def 默认值
     * @return 布尔值，如果转换失败则返回默认值
     */
    fun getBoolean(key: String, def: Boolean = false): Boolean {
        return get(key)?.cbool ?: def
    }

    /**
     * 检查是否包含指定键
     *
     * @param key 键名
     * @return 是否包含该键
     */
    fun containsKey(key: String): Boolean {
        return key in dataMap
    }

    /**
     * 检查是否包含指定标签
     *
     * @param tag 标签名
     * @return 是否包含该标签
     */
    fun containsTag(tag: String): Boolean {
        return tag in tags
    }

    override fun toString(): String {
        return "Demand(source='$source', namespace='$namespace', dataMap=$dataMap, args=$args, tags=$tags)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Demand) return false
        if (source != other.source) return false
        if (namespace != other.namespace) return false
        if (dataMap != other.dataMap) return false
        if (args != other.args) return false
        if (tags != other.tags) return false
        return true
    }

    override fun hashCode(): Int {
        var result = source.hashCode()
        result = 31 * result + namespace.hashCode()
        result = 31 * result + dataMap.hashCode()
        result = 31 * result + args.hashCode()
        result = 31 * result + tags.hashCode()
        return result
    }

    companion object {

        fun String.toDemand() = Demand(this)
    }
}