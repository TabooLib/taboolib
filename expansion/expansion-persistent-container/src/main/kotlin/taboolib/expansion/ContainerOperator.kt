package taboolib.expansion

import taboolib.module.database.Table
import taboolib.module.database.Where
import java.util.*
import javax.sql.DataSource

/**
 * Artifex
 * taboolib.expansion.ContainerOperator
 *
 * @author 坏黑
 * @since 2022/5/25 00:35
 */
abstract class ContainerOperator {

    abstract val table: Table<*, *>

    abstract val dataSource: DataSource

    abstract fun keys(uniqueId: UUID): List<String>

    abstract operator fun get(uniqueId: UUID): Map<String, Any?>

    abstract operator fun get(uniqueId: UUID, vararg rows: String): Map<String, Any?>

    abstract operator fun set(uniqueId: UUID, map: Map<String, Any?>)

    /**
     * 统一容器
     */
    abstract fun select(where: Where.() -> Unit): Map<String, Any?>

    abstract fun select(vararg rows: String, where: Where.() -> Unit): Map<String, Any?>

    abstract fun update(map: Map<String, Any?>, where: Where.() -> Unit)

    abstract fun insert(map: Map<String, Any?>)
}

