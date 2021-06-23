package taboolib.module.database

import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.SecuredFile
import java.io.File
import java.lang.Exception
import java.sql.Connection
import java.sql.ResultSet

fun File.getHost(): Host {
    return HostSQLite(this)
}

fun ConfigurationSection.getHost(name: String): Host {
    return HostSQL(getConfigurationSection(name) ?: SecuredFile())
}

fun <T> ResultSet.use(func: ResultSet.() -> T): T {
    return try {
        func(this)
    } catch (ex: Exception) {
        throw ex
    } finally {
        close()
    }
}

fun <T> Connection.use(func: Connection.() -> T): T {
    return try {
        func(this)
    } catch (ex: Exception) {
        throw ex
    } finally {
        close()
    }
}