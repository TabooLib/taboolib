package taboolib.module.database

import java.io.File

data class HostSQLite(val file: File) : Host<SQLite>() {

    override val columnBuilder: ColumnBuilder
        get() = SQLite()

    override val connectionUrl: String
        get() = "jdbc:sqlite:" + file.path

    override val connectionUrlSimple: String
        get() = "jdbc:sqlite:" + file.path

    override fun toString(): String {
        return "HostSQLite(file=$file, connectionUrl='$connectionUrl', connectionUrlSimple='$connectionUrlSimple')"
    }
}