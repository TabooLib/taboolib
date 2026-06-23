package taboolib.expansion.container

import taboolib.module.database.HostSQLite
import taboolib.module.database.SQLite
import java.io.File

class ContainerSQLite(file: File) : Container<SQLite>(HostSQLite(file)) {

    override val dialect: DatabaseDialect = SQLiteDialect
}
