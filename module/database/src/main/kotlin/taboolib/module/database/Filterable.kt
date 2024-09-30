package taboolib.module.database

/**
 * 过滤功能
 *
 * @author sky
 * @since 2021/6/24 1:58 上午
 */
abstract class Filterable {

    /**
     * 过滤标准
     */
    data class Criteria(
        // 数据库语句
        val query: String,
        // 元素
        val value: List<Any> = emptyList(),
        // 子集
        val children: List<Criteria> = emptyList()
    ) {

        /** 添加到 Filterable */
        fun apply(filterable: Filterable): Criteria {
            filterable.append(this)
            return this
        }
    }

    /**
     * 追加一个过滤标准
     */
    open fun append(criteria: Criteria) {
    }

    /**
     * 移除过滤标准
     */
    open fun remove(vararg criteria: Criteria) {
    }

    /** 或 */
    infix fun Criteria.or(other: Criteria): Criteria {
        return Criteria("(${query} OR ${other.query})", children = listOf(this, other)).apply {
            remove(this@or, other)
            append(this)
        }
    }

    /** 与 */
    infix fun Criteria.and(other: Criteria): Criteria {
        return Criteria("(${query} AND ${other.query})", children = listOf(this, other)).apply {
            remove(this@and, other)
            append(this)
        }
    }

    /**
     * 在某集合之内（支持 `pre` 作为非占位符的固定文本值）
     * ```
     * where {
     *    "value" inside(arrayOf(1, 2, pre("other_table.value")))
     * }
     * ```
     * 下面的懒得写了，都支持。
     */
    infix fun String.inside(value: Array<Any>): Criteria {
        if (value.isEmpty()) error("empty value")
        val el = arrayListOf<Any>()
        return Criteria("${asFormattedColumnName()} IN (${unwrapArray(value, el)})", el).apply(this@Filterable)
    }

    /** 在某范围之内 */
    infix fun String.between(value: Pair<Any, Any>): Criteria {
        val el = arrayListOf<Any>()
        return Criteria("${asFormattedColumnName()} BETWEEN ${unwrap(value.first, el)} AND ${unwrap(value.second, el)}", el).apply(this@Filterable)
    }

    /** 等于 */
    infix fun String.eq(value: Any?): Criteria {
        val el = arrayListOf<Any>()
        return if (value == null) {
            Criteria("${asFormattedColumnName()} IS NULL", el).apply(this@Filterable)
        } else {
            Criteria("${asFormattedColumnName()} = ${unwrap(value, el)}", el).apply(this@Filterable)
        }
    }

    /** 小于 */
    infix fun String.lt(value: Any): Criteria {
        val el = arrayListOf<Any>()
        return Criteria("${asFormattedColumnName()} < ${unwrap(value, el)}", el).apply(this@Filterable)
    }

    /** 小于等于 */
    infix fun String.lte(value: Any): Criteria {
        val el = arrayListOf<Any>()
        return Criteria("${asFormattedColumnName()} <= ${unwrap(value, el)}", el).apply(this@Filterable)
    }

    /** 大于 */
    infix fun String.gt(value: Any): Criteria {
        val el = arrayListOf<Any>()
        return Criteria("${asFormattedColumnName()} > ${unwrap(value, el)}", el).apply(this@Filterable)
    }

    /** 大于等于 */
    infix fun String.gte(value: Any): Criteria {
        val el = arrayListOf<Any>()
        return Criteria("${asFormattedColumnName()} >= ${unwrap(value, el)}", el).apply(this@Filterable)
    }

    /** 模糊匹配 */
    infix fun String.like(value: Any): Criteria {
        val el = arrayListOf<Any>()
        return Criteria("${asFormattedColumnName()} LIKE ${unwrap(value, el)}", el).apply(this@Filterable)
    }

    /** 否定 */
    fun not(func: Criteria): Criteria {
        return func.copy(query = "NOT (${func.query})")
    }

    /** 或 */
    fun or(func: Filter.() -> Unit): Criteria {
        val filter = Filter().also(func)
        if (filter.criteria.isEmpty()) error("empty function")
        return Criteria("(${filter.criteria.joinToString(" OR ") { it.query }})", children = filter.criteria).apply(this)
    }

    /** 与 */
    fun and(func: Filter.() -> Unit): Criteria {
        val filter = Filter().also(func)
        if (filter.criteria.isEmpty()) error("empty function")
        return Criteria("(${filter.criteria.joinToString(" AND ") { it.query }})", children = filter.criteria).apply(this)
    }

    /**
     * 创建一个非占位符的值，例如：
     * ```
     * where {
     *    "id" eq pre("other_table.id")
     * }
     * ```
     */
    fun pre(any: Any): PreValue {
        return PreValue(any)
    }

    private fun unwrapArray(value: Array<Any>, el: MutableList<Any>? = null): String {
        return value.joinToString { unwrap(it, el) }
    }

    private fun unwrap(value: Any, el: MutableList<Any>? = null): String {
        return if (value is PreValue) {
            value.asFormattedColumnName()
        } else {
            el?.add(value)
            "?"
        }
    }
}

/** 向下兼容 */
@Deprecated("Use Filterable instead.", ReplaceWith("Filterable"))
typealias WhereExecutor = Filterable

/** 向下兼容 */
@Deprecated("Use Filterable.Criteria instead.", ReplaceWith("Filterable.Criteria"))
typealias WhereData = Filterable.Criteria

/** 向下兼容 */
@Deprecated("Use Filter instead.", ReplaceWith("Filter"))
typealias Where = Filter