package taboolib.module.database

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

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

fun <T> PreparedStatement.use(func: PreparedStatement.() -> T): T {
    return try {
        func(this)
    } catch (ex: Exception) {
        throw ex
    } finally {
        close()
    }
}

/**
 * 尝试格式化一个列名
 */
internal fun Any.asFormattedColumnName(): String {
    val str = this.toString()
    // 如果字符串是 "null" || 如果字符串是一个函数或表达式 || 如果字符串已经被格式化
    if (str == "null" || str.contains(Regex("\\(.+\\)")) || (str.startsWith("`") && str.endsWith("`"))) {
        return str
    }
    // 通过 "." 分割字符串并分别格式化每个部分
    val parts = str.split(".")
    return parts.joinToString(".") { "`$it`" }
}