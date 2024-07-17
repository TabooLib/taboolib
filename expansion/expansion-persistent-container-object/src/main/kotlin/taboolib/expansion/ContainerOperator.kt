package taboolib.expansion

import taboolib.module.database.Filter
import taboolib.module.database.Table
import java.util.*
import javax.sql.DataSource

/**
 * ## 创建数据类
 * ```kotlin
 * // 数据类中必须存在
 * data class PlayerHome(
 *     @Id         // 主键（不是数据库意义上的主键，而是用于查询的主键）
 *     val username: UUID,
 *     @Key        // 建立索引（作用在于 update 时，若 @Id 重复出现，可以借助 @Key 来精确定位）
 *     @Length(32) // 数据在数据库中的长度
 *     val serverName: String,
 *     @Length(32)
 *     var world: String,
 *     var x: Double,
 *     var y: Double,
 *     var z: Double,
 *     var yaw: Float,
 *     var pitch: Float,
 *     var active: Boolean,
 * ) {
 *
 *     // 在数据类生成时，会优先查找解包函数，如果不存在则调用构造函数
 *     // 对于解包函数的要求：
 *     // 1. 名字任意
 *     // 2. 有且仅有一个 BundleMap 参数
 *     // 3. 返回值为该数据类
 *     companion object {
 *
 *         @JvmStatic
 *         fun wrap(map: BundleMap): PlayerFurniture {
 *             return PlayerHome(
 *                 map["username"],
 *                 map["server_name"], // serverName -> server_name
 *                 map["world"],
 *                 map["x"],
 *                 map["y"],
 *                 map["z"],
 *                 map["yaw"],
 *                 map["pitch"],
 *                 map["active"],
 *             )
 *         }
 *     }
 * }
 * ```
 *
 * ## 创建容器
 * ```kotlin
 * val container = persistentContainer { new<PlayerHome>() } // PlayerHome -> player_home
 *
 * // 别忘记释放资源
 * fun close() {
 *     container.close()
 * }
 * ```
 *
 * ## 获取数据
 * ```kotlin
 * // 获取所有家
 * fun getPlayerHomes(username: UUID): List<PlayerHome> {
 *     return container.get<PlayerHome>().find(username)
 * }
 *
 * // 获取特定服务器中的家
 * fun getPlayerHomes(username: UUID, serverName: String): List<PlayerHome> {
 *     return container.get<PlayerHome>().find(username) {
 *         "server_name" eq serverName
 *     }
 * }
 * ```
 *
 * ## 更新数据
 * ```kotlin
 * // 借助 @Id 和 @Key 定位数据并更新
 * // 数据类中必须存在可变数据（var）才可执行 update 函数，因为该函数仅更新可变数据
 * fun updatePlayerHome(home: PlayerHome) {
 *     container.get<PlayerHome>().updateByKey(home)
 * }
 *
 * // 借助 @Id 和自定义条件定位数据并更新（不借助 @Key）
 * fun updatePlayerHome(home: PlayerHome, serverName: String) {
 *     container.get<PlayerHome>().update(home) {
 *         "server_name" eq serverName
 *     }
 * }
 * ```
 *
 * @author 坏黑
 * @since 2022/5/25 00:35
 */
abstract class ContainerOperator {

    abstract val table: Table<*, *>

    abstract val dataSource: DataSource

    /**
     * 获取数据，获取一个，有多个仅返回第一个（默认不经过任何条件判断）
     *
     * @param filter 条件过滤器
     */
    inline fun <reified T> getOne(noinline filter: Filter.() -> Unit = {}): T? {
        return getOne(T::class.java, filter)
    }

    /**
     * 获取数据，获取多个（默认不经过任何条件判断）
     *
     * @param filter 条件过滤器
     */
    inline fun <reified T> get(noinline filter: Filter.() -> Unit = {}): List<T> {
        return get(T::class.java, filter)
    }

    /**
     * 查询数据，查一个，有多个仅返回第一个
     *
     * @param id 数据类中的 @Id
     * @param filter 条件过滤器
     */
    inline fun <reified T> findOne(id: Any, noinline filter: Filter.() -> Unit = {}): T? {
        return findOne(T::class.java, id, filter)
    }

    /**
     * 查询数据，查多个
     *
     * @param id 数据类中的 @Id
     * @param filter 条件过滤器
     */
    inline fun <reified T> find(id: Any, noinline filter: Filter.() -> Unit = {}): List<T> {
        return find(T::class.java, id, filter)
    }

    /**
     * 正序排序
     *
     * @param row 排序的列
     * @param limit 限制返回的数量
     * @param filter 条件过滤器
     */
    inline fun <reified T> sort(row: String, limit: Int = 10, noinline filter: Filter.() -> Unit = {}): List<T> {
        return sort(T::class.java, row, limit, filter)
    }

    /**
     * 倒序排序
     *
     * @param row 排序的列
     * @param limit 限制返回的数量
     * @param filter 条件过滤器
     */
    inline fun <reified T> sortDescending(row: String, limit: Int = 10, noinline filter: Filter.() -> Unit = {}): List<T> {
        return sortDescending(T::class.java, row, limit, filter)
    }

    /**
     * 检查数据是否存在
     *
     * @param id 数据类中的 @Id
     * @param filter 条件过滤器
     */
    inline fun <reified T> has(id: Any, noinline filter: Filter.() -> Unit = {}): Boolean {
        return has(T::class.java, id, filter)
    }

    /**
     * 删除数据
     *
     * @param id 数据类中的 @Id
     * @param filter 条件过滤器
     */
    inline fun <reified T> delete(id: Any, noinline filter: Filter.() -> Unit = {}) {
        return delete(T::class.java, id, filter)
    }

    /**
     * 获取数据，获取一个，有多个仅返回第一个（默认不经过任何条件判断）
     */
    abstract fun <T> getOne(type: Class<T>, filter: Filter.() -> Unit = {}): T?

    /**
     * 获取数据，获取多个（默认不经过任何条件判断）
     */
    abstract fun <T> get(type: Class<T>, filter: Filter.() -> Unit = {}): List<T>

    /**
     * 查询数据，查一个，有多个仅返回第一个
     */
    abstract fun <T> findOne(type: Class<T>, id: Any, filter: Filter.() -> Unit = {}): T?

    /**
     * 查询数据，查多个
     */
    abstract fun <T> find(type: Class<T>, id: Any, filter: Filter.() -> Unit = {}): List<T>

    /**
     * 正序排序
     */
    abstract fun <T> sort(type: Class<T>, row: String, limit: Int = 10, filter: Filter.() -> Unit = {}): List<T>

    /**
     * 倒序排序
     */
    abstract fun <T> sortDescending(type: Class<T>, row: String, limit: Int = 10, filter: Filter.() -> Unit = {}): List<T>

    /**
     * 更新数据，借助 @Id 定位数据并更新
     *
     * @param data 数据类
     */
    abstract fun update(data: Any, filter: Filter.() -> Unit = {})

    /**
     * 更新数据，借助 @Id 和 @Key 定位数据并更新
     *
     * @param data 数据类
     */
    abstract fun updateByKey(data: Any)

    /**
     * 插入数据
     */
    abstract fun insert(dataList: List<Any>)

    /**
     * 检查数据
     */
    abstract fun <T> has(type: Class<T>, id: Any, filter: Filter.() -> Unit = {}): Boolean

    /**
     * 检查数据
     */
    abstract fun has(filter: Filter.() -> Unit): Boolean

    /**
     * 删除数据
     */
    abstract fun <T> delete(type: Class<T>, id: Any, filter: Filter.() -> Unit = {})

    /**
     * 内部函数
     */
    protected fun Any.value(): Any {
        return when (this) {
            is UUID -> this.toString()
            is Char -> this.code
            else -> CustomTypeFactory.getCustomType(this)?.serialize(this) ?: this
        }
    }
}

