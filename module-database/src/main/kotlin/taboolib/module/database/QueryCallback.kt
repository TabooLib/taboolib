package taboolib.module.database

import java.sql.ResultSet

/**
 * TabooLib
 * taboolib.module.database.QueryCallback
 *
 * @author sky
 * @since 2021/6/23 2:11 下午
 */
open class QueryCallback() {

    open fun find() {

    }

    open fun <T> first(resultSet: ResultSet.() -> T): T {
        error(1)
    }

    open fun <T> map(resultSet: ResultSet.() -> T): List<T> {
        error(1)
    }

    open fun forEach(resultSet: ResultSet.() -> Unit) {
        error(1)
    }
}