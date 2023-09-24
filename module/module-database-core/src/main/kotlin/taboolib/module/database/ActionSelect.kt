package taboolib.module.database

/**
 * TabooLib
 * taboolib.module.database.ActionSelect
 *
 * @author sky
 * @since 2021/6/23 5:07 下午
 */
class ActionSelect(val table: String) : ActionFilterable() {

    /** 去除重复 */
    private var distincts = arrayListOf<String>()

    /** 查询行 */
    private var rows = arrayListOf("*")

    /** 连接 */
    private val join = arrayListOf<Join>()

    /** 排序 */
    private val order = arrayListOf<Order>()

    /** 限制 */
    private var limit = -1

    /** 语句 */
    override val query: String
        get() = Statement("SELECT")
            .addSegmentIfTrue(rows.isNotEmpty()) {
                addKeys(rows.toTypedArray(), false)
            }
            .addSegmentIfTrue(distincts.isNotEmpty()) {
                addSegment("DISTINCT")
                addKeys(distincts.toTypedArray(), false)
            }
            .addSegment("FROM")
            .addSegment(table.asFormattedColumnName())
            .addSegmentIfTrue(join.isNotEmpty()) {
                addOperations(join, separator = " ")
            }
            .addFilter(filter)
            .addSegmentIfTrue(order.isNotEmpty()) {
                addSegment("ORDER BY")
                addOperations(order)
            }
            .addSegmentIfTrue(limit > 0) {
                addSegment("LIMIT $limit")
            }.build()

    /** 元素 */
    override val elements: List<Any>
        get() {
            val el = ArrayList<Any>()
            el.addAll(join.flatMap { it.elements })
            el.addAll(filter?.elements ?: emptyList())
            return el
        }

    /**
     * 选择并返回表中关于 [row] 的所有数据，与 [distincts] 互斥
     */
    fun rows(vararg row: String) {
        rows.clear()
        rows += row
        distincts.clear()
    }

    /**
     * 选择并返回表中关于 [distinct] 的非重复数据，与 [rows] 互斥
     */
    fun distinct(vararg distinct: String) {
        rows.clear()
        distincts += distinct
    }

    /**
     * 排序（老版本写法）
     */
    @Deprecated("use orderBy instead", ReplaceWith("orderBy(row)"))
    fun order(row: String, desc: Boolean = false) {
        this.order += Order(row, if (desc) Order.Type.DESC else Order.Type.ASC)
    }

    /**
     * 排序
     */
    fun orderBy(row: String, type: Order.Type = Order.Type.ASC) {
        this.order += Order(row, type)
    }

    /**
     * 数量限制
     */
    fun limit(limit: Int) {
        this.limit = limit
    }

    /**
     * 内连接（两表的交集）
     */
    fun innerJoin(table: String, func: Filter.() -> Unit) {
        join += Join(JoinType.INNER, table, Filter().also(func))
    }

    /**
     * 左连接（左表的每一行都会加上右表中符合条件的行）
     */
    fun leftJoin(table: String, func: Filter.() -> Unit) {
        join += Join(JoinType.LEFT, table, Filter().also(func))
    }

    /**
     * 右连接（右表的每一行都会加上左表中符合条件的行）
     */
    fun rightJoin(table: String, func: Filter.() -> Unit) {
        join += Join(JoinType.RIGHT, table, Filter().also(func))
    }
}