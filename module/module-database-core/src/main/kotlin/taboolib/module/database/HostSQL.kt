package taboolib.module.database

import taboolib.library.configuration.ConfigurationSection

/**
 * SQL 数据库地址
 *
 * @author sky
 * @since 2018-05-14 19:01
 */
class HostSQL(val host: String, val port: String, val user: String, val password: String, val database: String) : Host<SQL>() {

    // allowPublicKeyRetrieval=true 用来针对 MySQL8 版本出现的 Public Key Retrieval is not allowed 异常
    val flags = arrayListOf("characterEncoding=utf-8", "useSSL=false", "allowPublicKeyRetrieval=true")

    val flagsURL: String
        get() = if (flags.isEmpty()) "" else "?${flags.joinToString("&")}"

    override val columnBuilder: ColumnBuilder
        get() = SQL()

    override val connectionUrl: String
        get() = "jdbc:mysql://$host:$port/$database$flagsURL"

    override val connectionUrlSimple: String
        get() = "jdbc:mysql://$host:$port/$database"

    constructor(section: ConfigurationSection) : this(
        section.getString("host", "localhost")!!,
        section.getString("port", "3306")!!,
        section.getString("user", "root")!!,
        section.getString("password", "root")!!,
        section.getString("database", "test")!!,
    )

    override fun toString(): String {
        return "HostSQL(host='$host', port='$port', user='$user', password='$password', database='$database', flags=$flags, flagsURL='$flagsURL', connectionUrl='$connectionUrl', connectionUrlSimple='$connectionUrlSimple')"
    }
}