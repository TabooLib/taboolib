package taboolib.expansion

import taboolib.module.database.ColumnOptionSQL
import taboolib.module.database.ColumnTypeSQL
import taboolib.module.database.HostSQL
import taboolib.module.database.Table

class ContainerSQL(host: String, port: Int, user: String, password: String, database: String) : Container() {

    override val host = HostSQL(host, port.toString(), user, password, database)

    override fun createTable(name: String, player: Boolean, playerKey: Boolean, data: List<ContainerBuilder.Data>): Table<*, *> {
        return Table(name, host) {
            add { id() }
            // 玩家容器
            if (player) {
                add("username") {
                    type(ColumnTypeSQL.CHAR, 36) {
                        options(if (playerKey) ColumnOptionSQL.UNIQUE_KEY else ColumnOptionSQL.KEY)
                    }
                }
            }
            data.forEach {
                add(it.name) {
                    val type = when {
                        it.int -> ColumnTypeSQL.INT
                        it.long -> ColumnTypeSQL.BIGINT
                        it.double -> ColumnTypeSQL.DOUBLE
                        else -> ColumnTypeSQL.VARCHAR
                    }
                    type(type, if (type == ColumnTypeSQL.VARCHAR) it.length else 0) {
                        if (it.key) {
                            options(ColumnOptionSQL.KEY)
                        }
                    }
                }
            }
        }
    }
}