package taboolib.module.database

/**
 * TabooLib
 * taboolib.module.database.Query
 *
 * @author 坏黑
 * @since 2023/9/22 13:07
 */
class Statement {

    constructor()
    constructor(namespace: String) {
        query += namespace
    }

    /** 语句 */
    val query = arrayListOf<String>()

    /** 追加语句 */
    fun addSegment(literal: String): Statement {
        if (literal.isNotEmpty()) {
            query += literal
        }
        return this
    }

    /** 追加语句 */
    fun addSegment(literal: List<String>): Statement {
        query += literal
        return this
    }

    /** 追加语句 */
    fun addSegmentIfTrue(predicate: Boolean, builder: Statement.() -> Unit): Statement {
        if (predicate) {
            query += Statement().also(builder).query.joinToString(" ")
        }
        return this
    }

    /** 追加语句 */
    fun addSegmentSequence(prefix: String = "", suffix: String = "", builder: Statement.() -> Unit): Statement {
        val children = Statement().also(builder).query
        if (children.isNotEmpty()) {
            query += "$prefix${children.joinToString()}$suffix"
        }
        return this
    }

    /** 追加过滤 */
    fun addFilter(filter: Filter? = null): Statement {
        if (filter != null && filter.isNotEmpty()) {
            addSegment("WHERE")
            addSegment(filter.query)
        }
        return this
    }

    /** 追加键 */
    fun addKeys(keys: Array<String>, useBrackets: Boolean = true, separator: CharSequence = ", "): Statement {
        val formattedKeys = keys.joinToString(separator) { it.asFormattedColumnName() }
        query += (if (useBrackets) "($formattedKeys)" else formattedKeys)
        return this
    }

    /** 追加值 */
    fun addValue(values: Array<Any>): Statement {
        query += "(${values.joinToString { "?" }})"
        return this
    }

    /** 追加值 */
    fun addValues(values: List<Array<Any?>>): Statement {
        query += values.joinToString { "(${it.joinToString { "?" }})" }
        return this
    }

    /** 追加特殊值 */
    fun addSpecialValue(value: Any): Statement {
        query += (if (value is String && value.startsWith('$')) value.substring(1) else "'$value'")
        return this
    }

    /** 追加操作 */
    fun addOperations(operation: List<Attributes>, separator: CharSequence = ", "): Statement {
        query += operation.joinToString(separator) { it.query }
        return this
    }

    /** 完成语句 */
    fun build(): String {
        return query.joinToString(" ")
    }
}