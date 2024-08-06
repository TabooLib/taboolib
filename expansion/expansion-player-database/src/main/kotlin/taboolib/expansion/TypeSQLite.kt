package taboolib.expansion

import taboolib.common.io.newFile
import taboolib.common.platform.function.pluginId
import taboolib.module.database.ColumnTypeSQLite
import taboolib.module.database.Host
import taboolib.module.database.Table
import taboolib.module.database.getHost
import java.io.File

class TypeSQLite(file: File, tableName: String? = null) : Type() {

    val host = newFile(file).getHost()

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

    override fun host(): Host<*> {
        return host
    }

    override fun tableVar(): Table<*, *> {
        return tableVar
    }
}
