package taboolib.expansion

import taboolib.common.io.newFile
import taboolib.common.platform.function.pluginId
import taboolib.module.database.ColumnTypeSQLite
import taboolib.module.database.Host
import taboolib.module.database.Table
import taboolib.module.database.getHost
import java.io.File

/**
 * SQLite 数据类型实现类。
 *
 * @property file 数据库文件
 * @property tableName 表名，如果为空则使用插件 ID
 */
class TypeSQLite(val file: File, val tableName: String? = null) : Type() {

    /**
     * SQLite 数据库主机
     */
    val host = newFile(file).getHost()

    /**
     * 数据表结构
     */
    val tableVar = Table(tableName ?: pluginId, host) {
        add("user") {
            type(ColumnTypeSQLite.TEXT, 64)
        }
        add("key") {
            type(ColumnTypeSQLite.TEXT, 64)
        }
        add("value") {
            type(ColumnTypeSQLite.TEXT)
        }
    }

    /**
     * 获取数据库主机
     *
     * @return 数据库主机
     */
    override fun host(): Host<*> {
        return host
    }

    /**
     * 获取数据表结构
     *
     * @return 数据表结构
     */
    override fun tableVar(): Table<*, *> {
        return tableVar
    }
}