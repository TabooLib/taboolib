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
internal fun Any.formatColumn(): String {
    var isColumnName = false
    var escape = false
    val name = toString().toCharArray().joinToString("") {
        when (it) {
            // 若出现「`」则认为是列名
            '`' -> {
                isColumnName = true
                it.toString()
            }
            // 转义符号
            '\\' -> {
                if (escape) {
                    "\\"
                } else {
                    escape = true
                    ""
                }
            }
            '.' -> {
                if (escape) {
                    it.toString()
                } else {
                    "`.`"
                }
            }
            else -> it.toString()
        }
    }
    return if (isColumnName) name else "`$name`"
}