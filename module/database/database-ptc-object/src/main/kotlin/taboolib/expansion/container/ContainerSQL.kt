package taboolib.expansion.container

import taboolib.module.database.HostSQL
import taboolib.module.database.SQL

class ContainerSQL(
    host: String,
    port: Int,
    user: String,
    password: String,
    database: String,
    flags: List<String> = emptyList(),
    clearFlags: Boolean = false,
    ssl: String? = null,
) : Container<SQL>(HostSQL(host, port.toString(), user, password, database).also {
    if (clearFlags) {
        it.flags.clear()
    }
    it.flags.addAll(flags)
    if (ssl != null) {
        it.flags -= "useSSL=false"
        it.flags += "sslMode=$ssl"
    }
}) {

    override val dialect: DatabaseDialect = MySQLDialect
}
