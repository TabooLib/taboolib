package taboolib.expansion

import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import taboolib.module.database.ColumnBuilder
import taboolib.module.database.Host
import taboolib.module.database.Table
import java.util.concurrent.ConcurrentHashMap

abstract class Container<T : ColumnBuilder>(val host: Host<T>) {

    val map = ConcurrentHashMap<String, ContainerOperator>()
    val dataSource = host.createDataSource(autoRelease = false)

    /** 创建表 */
    protected abstract fun createTableObject(type: AnalyzedClass, name: String): Table<*, *>

    /** 创建表 */
    open fun createTable(type: AnalyzedClass, name: String) {
        map[name] = ContainerOperatorImpl(createTableObject(type, name), dataSource)
    }

    /** 初始化所有表 */
    open fun init() {
        map.forEach { it.value.table.createTable(dataSource) }
    }

    /** 获取路径 */
    open fun path(): String {
        return host.connectionUrl.toString()
    }

    /** 关闭连接 */
    open fun close() {
        dataSource.invokeMethod<Void>("close")
    }

    /** 获取控制器 */
    open fun operator(name: String): ContainerOperator {
        return map[name] ?: error("Table not found")
    }
}