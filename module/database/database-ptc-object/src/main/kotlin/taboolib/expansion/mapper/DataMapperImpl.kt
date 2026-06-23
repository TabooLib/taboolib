package taboolib.expansion.mapper

import taboolib.expansion.*

/**
 * DataMapper 的标准实现
 *
 * 封装 ContainerOperator，提供类型安全的 CRUD 操作。
 * 支持可选的 L2 双层缓存。
 *
 * ### L2 缓存架构
 *
 * - **Bean Cache**：按实体 ID 存储，细粒度失效（更新/删除只影响特定 ID）
 * - **Query Cache**：按查询哈希存储，粗粒度失效（任何写操作清空整个 Query Cache）
 *
 * ### 缓存失效策略
 *
 * | 操作类型 | Bean Cache | Query Cache |
 * |---------|-----------|-------------|
 * | 插入 | 不影响 | 全部清空 |
 * | 单条更新/删除 | 仅失效该 ID | 全部清空 |
 * | 批量/不确定范围 | 全部清空 | 全部清空 |
 *
 * @param type 数据类的 Class 对象
 * @param container 持久化容器
 * @param cache L2 双层缓存（可选）
 */
class DataMapperImpl<T>(
    override val type: Class<T>,
    private val container: PersistentContainer,
    override val cache: L2Cache?
) : AbstractDataMapper<T>() {

    override val operator: ContainerOperator
        get() = container[container.resolveTableName(type)]

    // 缓存读取：覆写分派方法，走 L2 缓存
    override fun <R> executeRead(key: String, vararg args: Any?, query: () -> R): R {
        return cachedQuery(key, *args) { query() }
    }

    override fun <R> executeBeanRead(key: String, vararg args: Any?, query: () -> R): R {
        return cachedBean(key, *args) { query() }
    }

    // 游标查询在 DataMapperImpl 中不支持（基类默认实现已抛错）

    // === 事务 ===

    override fun <R> transaction(block: DataMapper<T>.() -> R): Result<R> {
        return container.transaction {
            val txOperator = operator(container.resolveTableName(type))
            val txMapper = TransactionalDataMapper(type, txOperator, cache, connection)
            txMapper.block()
        }
    }

    // === 多表联查 ===

    override fun join(builder: JoinQuery.() -> Unit): JoinQuery {
        return container.join {
            from(tableName)
            builder()
        }
    }

    // === 生命周期 ===

    override fun close() = container.close()
}
