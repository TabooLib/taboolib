package taboolib.expansion

import taboolib.common.platform.function.warning
import taboolib.common5.*
import taboolib.expansion.orm.AnalyzedClass
import taboolib.expansion.orm.AnalyzedClassMember
import taboolib.expansion.orm.AnalyzedClassMember.Companion.resolveTableName
import taboolib.expansion.orm.EntityMapper
import taboolib.module.database.ActionSelect
import taboolib.module.database.Filter
import taboolib.module.database.JoinFilter
import taboolib.module.database.Order
import taboolib.module.database.asFormattedColumnName
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*
import kotlin.Any
import kotlin.Array
import kotlin.Enum
import kotlin.Int
import kotlin.Pair
import kotlin.PublishedApi
import kotlin.String
import kotlin.Unit
import kotlin.also
import kotlin.apply
import kotlin.arrayOf
import kotlin.error
import kotlin.to
import kotlin.use

/**
 * 多表联查 DSL
 *
 * 通过 [PersistentContainer.join] 或 [TransactionContext.join] 创建。
 * 委托 [ActionSelect] 构建 SQL，支持 INNER/LEFT/RIGHT JOIN。
 *
 * ```kotlin
 * container.join {
 *     from<Player>()
 *     innerJoin<PlayerStats> {
 *         on("player.username" eq pre("player_stats.username"))
 *     }
 *     where { "player.active" eq true }
 *     limit(10)
 * }.execute()
 * ```
 *
 * @author sky
 */
class JoinQuery internal constructor(
    private val dataSource: javax.sql.DataSource,
    private val sharedConnection: Connection? = null,
    @PublishedApi internal val tablePrefix: String = ""
) {

    private var tableName: String? = null
    private val joins = mutableListOf<JoinDef>()
    private var columns: Array<String>? = null
    private var filterFunc: (Filter.() -> Unit)? = null
    private val orders = mutableListOf<Pair<String, Order.Type>>()
    @PublishedApi internal var limitValue = -1
    private var offsetValue = -1

    /** 获取带前缀的表名（供 join {} 块内使用） */
    inline fun <reified T> tableName(): String = tablePrefix + T::class.java.resolveTableName()

    /** 主表（泛型版，类名自动转 snake_case，并应用表前缀） */
    @JvmName("fromTyped")
    inline fun <reified T> from(name: String? = null): JoinQuery {
        return from(name ?: tableName<T>())
    }

    /** 主表 */
    fun from(name: String): JoinQuery {
        tableName = name
        return this
    }

    /** 内连接（泛型） */
    @JvmName("innerJoinTyped")
    inline fun <reified T> innerJoin(name: String? = null, noinline on: JoinFilter.() -> Unit): JoinQuery {
        return innerJoin(name ?: tableName<T>(), on)
    }

    /** 左连接（泛型） */
    @JvmName("leftJoinTyped")
    inline fun <reified T> leftJoin(name: String? = null, noinline on: JoinFilter.() -> Unit): JoinQuery {
        return leftJoin(name ?: tableName<T>(), on)
    }

    /** 右连接（泛型） */
    @JvmName("rightJoinTyped")
    inline fun <reified T> rightJoin(name: String? = null, noinline on: JoinFilter.() -> Unit): JoinQuery {
        return rightJoin(name ?: tableName<T>(), on)
    }

    /** 内连接 */
    fun innerJoin(name: String, on: JoinFilter.() -> Unit): JoinQuery {
        joins += JoinDef(JoinDefType.INNER, name, on)
        return this
    }

    /** 左连接 */
    fun leftJoin(name: String, on: JoinFilter.() -> Unit): JoinQuery {
        joins += JoinDef(JoinDefType.LEFT, name, on)
        return this
    }

    /** 右连接 */
    fun rightJoin(name: String, on: JoinFilter.() -> Unit): JoinQuery {
        joins += JoinDef(JoinDefType.RIGHT, name, on)
        return this
    }

    /** 内连接（子查询） */
    fun innerJoin(sub: SubQuery, on: JoinFilter.() -> Unit): JoinQuery {
        joins += JoinDef(JoinDefType.INNER, sub.sql, on, sub.params)
        return this
    }

    /** 左连接（子查询） */
    fun leftJoin(sub: SubQuery, on: JoinFilter.() -> Unit): JoinQuery {
        joins += JoinDef(JoinDefType.LEFT, sub.sql, on, sub.params)
        return this
    }

    /** 右连接（子查询） */
    fun rightJoin(sub: SubQuery, on: JoinFilter.() -> Unit): JoinQuery {
        joins += JoinDef(JoinDefType.RIGHT, sub.sql, on, sub.params)
        return this
    }

    /** 内连接（原始 SQL + 参数） */
    fun innerJoin(name: String, params: List<Any>, on: JoinFilter.() -> Unit): JoinQuery {
        joins += JoinDef(JoinDefType.INNER, name, on, params)
        return this
    }

    /** 左连接（原始 SQL + 参数） */
    fun leftJoin(name: String, params: List<Any>, on: JoinFilter.() -> Unit): JoinQuery {
        joins += JoinDef(JoinDefType.LEFT, name, on, params)
        return this
    }

    /** 右连接（原始 SQL + 参数） */
    fun rightJoin(name: String, params: List<Any>, on: JoinFilter.() -> Unit): JoinQuery {
        joins += JoinDef(JoinDefType.RIGHT, name, on, params)
        return this
    }

    /** 指定查询列 */
    fun select(vararg rows: String): JoinQuery {
        columns = arrayOf(*rows)
        return this
    }

    /**
     * 指定查询列并设置别名
     *
     * 解决多表联查中同名列冲突问题。别名将作为 BundleMap 的 key，
     * 同时也是 mapTo 匹配 data class 字段名的依据。
     *
     * ```kotlin
     * homeTable.join {
     *     innerJoin<PlayerStats> {
     *         on("player_home.username" eq pre("player_stats.username"))
     *     }
     *     selectAs(
     *         "player_home.username" to "username",
     *         "player_home.world" to "world",
     *         "player_stats.level" to "level",
     *         "player_stats.username" to "stats_username"
     *     )
     * }.mapTo<PlayerSummary>()
     * // PlayerSummary(val username: String, val world: String, val level: Int, val statsUsername: String)
     * ```
     *
     * @param pairs 列名 to 别名
     */
    fun selectAs(vararg pairs: Pair<String, String>): JoinQuery {
        columns = pairs.map { (col, alias) ->
            val formattedCol = col.asFormattedColumnName()
            "$formattedCol AS ${alias.asFormattedColumnName()}"
        }.toTypedArray()
        return this
    }

    /** WHERE 条件 */
    fun where(filter: Filter.() -> Unit): JoinQuery {
        filterFunc = filter
        return this
    }

    /** 排序 */
    fun orderBy(row: String, type: Order.Type = Order.Type.ASC): JoinQuery {
        orders += row to type
        return this
    }

    /** 数量限制 */
    fun limit(limit: Int): JoinQuery {
        limitValue = limit
        return this
    }

    /** 偏移量 */
    fun offset(offset: Int): JoinQuery {
        offsetValue = offset
        return this
    }

    /** 执行查询，返回 BundleMap 列表 */
    fun execute(): List<BundleMap> {
        return executeQuery { rs ->
            val meta = rs.metaData
            val columnCount = meta.columnCount
            buildList {
                while (rs.next()) {
                    val map = linkedMapOf<String, Any?>()
                    for (i in 1..columnCount) {
                        map[meta.getColumnLabel(i)] = rs.getObject(i)
                    }
                    add(BundleMapImpl(map))
                }
            }
        }
    }

    /** 执行查询，自定义映射 */
    fun <T> map(mapper: (BundleMap) -> T): List<T> {
        return execute().map(mapper)
    }

    /** 执行查询，反射映射到 data class */
    inline fun <reified T> mapTo(): List<T> {
        return mapTo(T::class.java)
    }

    /** 执行查询，反射映射到 data class */
    fun <T> mapTo(type: Class<T>): List<T> {
        val analyzed = AnalyzedClass.of(type)
        val mapper = EntityMapper.of(type)
        return execute().map { bundle ->
            val map = hashMapOf<String, Any?>()
            analyzed.members.forEach { member ->
                val raw = (bundle as BundleMapImpl).map[member.name]
                map[member.name] = convertValue(member, raw)
            }
            mapper.createInstance(map)
        }
    }

    /** 执行查询，返回单条 BundleMap */
    fun executeOne(): BundleMap? {
        limitValue = 1
        return execute().firstOrNull()
    }

    /** 执行查询，返回单条 data class */
    inline fun <reified T> mapOneTo(): T? {
        limitValue = 1
        return mapTo<T>().firstOrNull()
    }

    // --- 内部实现 ---

    private fun buildAction(): ActionSelect {
        val table = tableName ?: error("JoinQuery requires from() to specify the primary table")
        return ActionSelect(table).apply {
            columns?.also { rows(*it) }
            joins.forEach { def ->
                when (def.type) {
                    JoinDefType.INNER -> innerJoin(def.table, def.params, def.on)
                    JoinDefType.LEFT -> leftJoin(def.table, def.params, def.on)
                    JoinDefType.RIGHT -> rightJoin(def.table, def.params, def.on)
                }
            }
            filterFunc?.also { where(it) }
            orders.forEach { (row, type) -> orderBy(row, type) }
            if (limitValue > 0) limit(limitValue)
            if (offsetValue > 0) offset(offsetValue)
        }
    }

    private fun <T> executeQuery(handler: (ResultSet) -> T): T {
        val action = buildAction()
        return if (sharedConnection != null) {
            executePrepared(sharedConnection, action, handler)
        } else {
            dataSource.connection.use { conn ->
                executePrepared(conn, action, handler)
            }
        }
    }

    private fun <T> executePrepared(conn: Connection, action: ActionSelect, handler: (ResultSet) -> T): T {
        return try {
            conn.prepareStatement(action.query).use { stmt ->
                action.elements.forEachIndexed { i, v -> stmt.setObject(i + 1, v) }
                stmt.executeQuery().use(handler)
            }
        } catch (ex: SQLException) {
            warning("Query: ${action.query}")
            warning("Parameters (${action.elements.size}): ${action.elements}")
            throw ex
        }
    }

    private fun convertValue(member: AnalyzedClassMember, raw: Any?): Any? {
        if (raw == null) return null
        return when {
            member.isBoolean -> raw.cbool
            member.isByte -> raw.cbyte
            member.isShort -> raw.cshort
            member.isInt -> raw.cint
            member.isLong -> raw.clong
            member.isFloat -> raw.cfloat
            member.isDouble -> raw.cdouble
            member.isChar -> raw.cint.toChar()
            member.isString -> raw.toString()
            member.isUUID -> UUID.fromString(raw.toString())
            member.isIndexedEnum -> {
                val idx = raw.clong
                member.returnType.enumConstants.firstOrNull { (it as IndexedEnum).index == idx }
            }
            member.isEnum -> member.returnType.enumConstants.firstOrNull { (it as Enum<*>).name == raw.toString() }
            member.isByteArray -> raw.cByteArray
            else -> {
                val customType = CustomTypeFactory.getCustomTypeByClass(member.returnType)
                customType?.deserialize(raw) ?: raw
            }
        }
    }

    private data class JoinDef(val type: JoinDefType, val table: String, val on: JoinFilter.() -> Unit, val params: List<Any> = emptyList())

    private enum class JoinDefType { INNER, LEFT, RIGHT }
}

/**
 * 子查询定义
 *
 * 复用 [ActionSelect] 构建 SQL 和收集参数，用于 [JoinQuery] 的子查询 JOIN。
 *
 * @see subQuery
 */
class SubQuery(val alias: String, private val action: ActionSelect) {

    /** 生成 `(SELECT ...) AS "alias"` 格式的 SQL 片段 */
    val sql: String get() = "(${action.query}) AS ${alias.asFormattedColumnName()}"

    /** 子查询中的参数列表 */
    val params: List<Any> get() = action.elements
}

/**
 * 构建子查询 DSL
 *
 * 复用 [ActionSelect] 的全部能力（rows/where/groupBy/orderBy/limit 等），
 * 自动收集参数用于 PreparedStatement 绑定。
 *
 * ```kotlin
 * homeMapper.join {
 *     from("`player_home` AS `h`")
 *     innerJoin(
 *         subQuery("player_stats", "sub") {
 *             rows("username", "SUM(kills) AS total_kills")
 *             where { "kills" gt 50 }
 *             groupBy("username")
 *         }
 *     ) {
 *         on("h.username" eq pre("sub.username"))
 *     }
 *     selectAs("h.username" to "username", "sub.total_kills" to "total_kills")
 *     where { "h.world" eq "world" }
 * }.execute()
 * ```
 *
 * @param table 子查询的源表名
 * @param alias 子查询的别名，用于外层 ON/WHERE/selectAs 引用
 * @param builder ActionSelect DSL
 */
fun subQuery(table: String, alias: String, builder: ActionSelect.() -> Unit): SubQuery {
    return SubQuery(alias, ActionSelect(table).apply(builder))
}
