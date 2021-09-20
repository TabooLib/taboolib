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
open class QueryTasks(tasks: List<QueryTask>) {

    val tasks = tasks.toMutableList()

    init {
        if (tasks.isEmpty()) {
            error("empty workspace")
        }
    }

    open fun run(): Int {
        val last = tasks.removeLast()
        tasks.forEach { it.future.call { fetchSize } }
        return last.future.call { fetchSize }
    }

    open fun find(): Boolean {
        val last = tasks.removeLast()
        tasks.forEach { it.future.call { fetchSize } }
        return last.future.call { next() }
    }

    open fun <T> first(resultSet: ResultSet.() -> T): T {
        val last = tasks.removeLast()
        tasks.forEach { it.future.call { fetchSize } }
        return last.future.call {
            next()
            resultSet(this)
        }
    }

    open fun <T> firstOrNull(resultSet: ResultSet.() -> T): T? {
        val last = tasks.removeLast()
        tasks.forEach { it.future.call { fetchSize } }
        return last.future.call {
            if (next()) {
                resultSet(this)
            } else {
                null
            }
        }
    }

    open fun <T> map(resultSet: ResultSet.() -> T): List<T> {
        val last = tasks.removeLast()
        tasks.forEach { it.future.call { fetchSize } }
        return last.future.call {
            ArrayList<T>().also {
                while (next()) {
                    it += resultSet(this)
                }
            }
        }
    }

    open fun forEach(resultSet: ResultSet.() -> Unit) {
        val last = tasks.removeLast()
        tasks.forEach { it.future.call { fetchSize } }
        last.future.call {
            while (next()) {
                resultSet(this)
            }
        }
    }

    open fun forEachIndexed(resultSet: ResultSet.(index: Int) -> Unit) {
        val last = tasks.removeLast()
        tasks.forEach { it.future.call { fetchSize } }
        last.future.call {
            var i = 0
            while (next()) {
                resultSet(this, i++)
            }
        }
    }
}