package taboolib.module.database

import java.sql.ResultSet

/**
 * TabooLib
 * taboolib.module.database.QueryTask
 *
 * @author sky
 * @since 2021/6/23 2:11 下午
 */
@Suppress("UNCHECKED_CAST")
open class QueryTask(val future: Future<ResultSet>) {

    open fun run(): Int {
        return future.call { fetchSize }
    }

    open fun find(): Boolean {
        return future.call { next() }
    }

    open fun <T> first(resultSet: ResultSet.() -> T): T {
        return future.call {
            next()
            resultSet(this)
        }
    }

    open fun <T> firstOrNull(resultSet: ResultSet.() -> T): T? {
        return future.call {
            if (next()) {
                resultSet(this)
            } else {
                null
            }
        }
    }

    open fun <T> map(resultSet: ResultSet.() -> T): List<T> {
        return future.call {
            ArrayList<T>().also {
                while (next()) {
                    it += resultSet(this)
                }
            }
        }
    }

    open fun forEach(resultSet: ResultSet.() -> Unit) {
        future.call {
            while (next()) {
                resultSet(this)
            }
        }
    }
}