package taboolib.module.database

import java.sql.ResultSet

/**
 * TabooLib
 * taboolib.module.database.QueryTask
 *
 * @author sky
 * @since 2021/6/23 2:11 下午
 */
open class QueryTask(val future: Future<ResultSet>) {

    open fun run(): Int {
        return future.call { fetchSize }
    }

    open fun find(): Boolean {
        return future.call { next() }
    }

    open fun <T> first(call: ResultSet.() -> T): T {
        return future.call {
            next()
            call(this)
        }
    }

    open fun <T> firstOrNull(call: ResultSet.() -> T): T? {
        return future.call {
            if (next()) {
                call(this)
            } else {
                null
            }
        }
    }

    open fun <T> map(call: ResultSet.() -> T): List<T> {
        return future.call {
            val arr = arrayListOf<T>()
            while (next()) {
                arr += call(this)
            }
            arr
        }
    }

    open fun <T> mapNotNull(call: ResultSet.() -> T?): List<T> {
        return future.call {
            val arr = arrayListOf<T>()
            while (next()) {
                call(this)?.also { t -> arr += t }
            }
            arr
        }
    }

    open fun forEach(call: ResultSet.() -> Unit) {
        future.call {
            while (next()) {
                call(this)
            }
        }
    }
}