package taboolib.expansion

import taboolib.module.database.Table
import taboolib.module.database.Where
import java.util.*
import javax.sql.DataSource

class ContainerOperatorFlatten(
    override val table: Table<*, *>,
    override val dataSource: DataSource,
    val key: String,
    val value: String,
) : ContainerOperator() {

    override fun keys(uniqueId: UUID): List<String> {
        return table.select(dataSource) {
            rows(key)
            where("username" eq uniqueId.toString())
        }.map { getString(key) }.toList()
    }

    override fun get(uniqueId: UUID): Map<String, Any?> {
        return table.select(dataSource) {
            rows(key, value)
            where("username" eq uniqueId.toString())
        }.map { getString(key) to getObject(value) }.toMap()
    }

    override fun get(uniqueId: UUID, vararg rows: String): Map<String, Any?> {
        return table.select(dataSource) {
            rows(key, value)
            where("username" eq uniqueId.toString() and (key inside arrayOf(*rows)))
        }.map { getString(key) to getObject(value) }.toMap()
    }

    override fun set(uniqueId: UUID, map: Map<String, Any?>) {
        val keys = keys(uniqueId)
        val updateMap = map.filterKeys { it in keys }
        val insertMap = map.filterKeys { it !in keys }
        // 更新数据
        updateMap.forEach { (k, v) ->
            table.update(dataSource) {
                where("username" eq uniqueId.toString() and (key eq k))
                set(value, v)
            }
        }
        // 插入数据
        if (insertMap.isNotEmpty()) {
            table.insert(dataSource, "username", key, value) {
                insertMap.filter { it.value != null }.forEach { (k, v) -> value(uniqueId.toString(), k, v!!) }
            }
        }
    }

    override fun select(where: Where.() -> Unit): Map<String, Any?> {
        error("Not supported in flatten container")
    }

    override fun select(vararg rows: String, where: Where.() -> Unit): Map<String, Any?> {
        error("Not supported in flatten container")
    }

    override fun update(map: Map<String, Any?>, where: Where.() -> Unit) {
        error("Not supported in flatten container")
    }

    override fun insert(map: Map<String, Any?>) {
        error("Not supported in flatten container")
    }
}