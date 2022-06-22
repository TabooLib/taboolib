package taboolib.expansion

import taboolib.module.database.ColumnOptionSQLite
import taboolib.module.database.ColumnTypeSQLite
import taboolib.module.database.HostSQLite
import taboolib.module.database.Table
import java.io.File

class ContainerSQLite(file: File) : Container() {

    override val host = HostSQLite(file)

    override fun createTable(name: String, player: Boolean, playerKey: Boolean, data: List<ContainerBuilder.Data>): Table<*, *> {
        return Table(name, host) {
            // 玩家容器
            if (player) {
                add("username") {
                    type(ColumnTypeSQLite.TEXT, 36) {
                        if (playerKey) {
                            options(ColumnOptionSQLite.PRIMARY_KEY)
                        }
                    }
                }
            }
            data.forEach {
                add(it.name) {
                    val type = when {
                        it.int || it.long -> ColumnTypeSQLite.INTEGER
                        it.double -> ColumnTypeSQLite.NUMERIC
                        else -> ColumnTypeSQLite.TEXT
                    }
                    type(type, if (type == ColumnTypeSQLite.TEXT) it.length else 0)
                }
            }
        }
    }
}