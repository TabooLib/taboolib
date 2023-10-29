package taboolib.expansion

import taboolib.module.database.Table
import taboolib.module.database.Filter
import java.util.*
import javax.sql.DataSource

class ContainerOperatorNormal(
    override val table: Table<*, *>,
    override val dataSource: DataSource,
    val player: Boolean,
    val data: List<ContainerBuilder.Data>,
) : ContainerOperator() {

    override fun keys(uniqueId: UUID): List<String> {
        error("Not supported in normal container")
    }

    override fun get(uniqueId: UUID): Map<String, Any?> {
        return if (player) {
            table.select(dataSource) {
                rows(*data.map { it.name }.toTypedArray())
                where("username" eq uniqueId.toString())
                limit(1)
            }.firstOrNull { data.associate { it.name to getObject(it.name) } } ?: emptyMap()
        } else {
            error("Not supported in no-player container")
        }
    }

    override fun get(uniqueId: UUID, vararg rows: String): Map<String, Any?> {
        return if (player) {
            table.select(dataSource) {
                rows(*rows)
                where("username" eq uniqueId.toString())
                limit(1)
            }.firstOrNull { rows.associateWith { getObject(it) } } ?: emptyMap()
        } else {
            error("Not supported in no-player container")
        }
    }

    override fun set(uniqueId: UUID, map: Map<String, Any?>) {
        if (player) {
            if (table.find(dataSource) { where("username" eq uniqueId.toString()) }) {
                table.update(dataSource) {
                    where("username" eq uniqueId.toString())
                    map.forEach { (k, v) -> set(k, v) }
                }
            } else {
                val newMap = map.filterValues { it != null }.map { it.key to it.value!! }.toMap()
                table.insert(dataSource, "username", *newMap.keys.toTypedArray()) {
                    value(uniqueId.toString(), *newMap.values.toTypedArray())
                }
            }
        } else {
            error("Not supported in no-player container")
        }
    }

    override fun select(filter: Filter.() -> Unit): Map<String, Any?> {
        return select(*data.map { it.name }.toTypedArray()) { filter() }
    }

    override fun select(vararg rows: String, filter: Filter.() -> Unit): Map<String, Any?> {
        return table.select(dataSource) {
            rows(*rows)
            where(filter)
            limit(1)
        }.firstOrNull { rows.associateWith { getObject(it) } } ?: emptyMap()
    }

    override fun selectAll(filter: Filter.() -> Unit): List<Map<String, Any?>> {
        return selectAll(*data.map { it.name }.toTypedArray()) { filter() }
    }

    override fun selectAll(vararg rows: String, filter: Filter.() -> Unit): List<Map<String, Any?>> {
        return table.select(dataSource) {
            rows(*rows)
            where(filter)
        }.map { rows.associateWith { getObject(it) } }
    }

    override fun update(map: Map<String, Any?>, filter: Filter.() -> Unit) {
        if (table.find(dataSource) { where(filter) }) {
            table.update(dataSource) {
                where(filter)
                map.forEach { (k, v) -> set(k, v) }
            }
        } else {
            table.insert(dataSource, *map.filter { it.value != null }.keys.toTypedArray()) {
                value(*map.values.filterNotNull().toTypedArray())
            }
        }
    }

    override fun insert(map: Map<String, Any?>) {
        table.insert(dataSource, *map.filter { it.value != null }.keys.toTypedArray()) {
            value(*map.values.filterNotNull().toTypedArray())
        }
    }
}