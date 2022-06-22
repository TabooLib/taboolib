package taboolib.expansion

import taboolib.common.reflect.Reflex.Companion.invokeMethod
import taboolib.module.database.Host
import taboolib.module.database.Table

abstract class Container {

    abstract val host: Host<*>

    val hostTables = HashMap<String, Table<*, *>>()
    val hostTableOperator = HashMap<String, ContainerOperator>()
    val dataSource by lazy { host.createDataSource(autoRelease = false) }

    abstract fun createTable(name: String, player: Boolean, playerKey: Boolean, data: List<ContainerBuilder.Data>): Table<*, *>

    /**
     * 添加数据表
     */
    open fun addTable(name: String, player: Boolean, playerKey: Boolean, data: List<ContainerBuilder.Data>) {
        val table = createTable(name, player, playerKey, data).also { hostTables[name] = it }
        // 扁平容器
        if (player && !playerKey && data.size == 2) {
            hostTableOperator[name] = ContainerOperatorFlatten(table, dataSource, data[0].name, data[1].name)
        } else {
            hostTableOperator[name] = ContainerOperatorNormal(table, dataSource, player, data)
        }
    }

    open fun init() {
        hostTables.forEach { it.value.createTable(dataSource) }
    }

    open fun path(): String {
        return host.connectionUrl.toString()
    }

    open fun close() {
        dataSource.invokeMethod<Void>("close")
    }

    open fun operator(name: String): ContainerOperator {
        return hostTableOperator[name] ?: error("Table not found")
    }
}