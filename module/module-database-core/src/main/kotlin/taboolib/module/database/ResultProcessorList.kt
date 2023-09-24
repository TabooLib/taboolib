package taboolib.module.database

import java.sql.ResultSet

/**
 * 多结果处理器
 *
 * @author sky
 * @since 2021/6/23 2:11 下午
 */
open class ResultProcessorList(processors: List<ResultProcessor>, val transaction: Boolean = false) {

    val processors = processors.toMutableList()

    /** 是否已经执行 */
    var isExecuted = false
        private set

    init {
        if (processors.isEmpty()) {
            error("processors is empty")
        }
    }

    open fun run(): Int {
        if (isExecuted) {
            error("processors is already executed")
        }
        isExecuted = true
        val last = processors.removeLast()
        processors.forEach { it.executor.invoke { fetchSize } }
        return last.executor.invoke { fetchSize }
    }

    open fun find(): Boolean {
        if (isExecuted) {
            error("processors is already executed")
        }
        isExecuted = true
        val last = processors.removeLast()
        processors.forEach { it.executor.invoke { fetchSize } }
        return last.executor.invoke { next() }
    }

    open fun <T> first(resultSet: ResultSet.() -> T): T {
        if (isExecuted) {
            error("processors is already executed")
        }
        isExecuted = true
        val last = processors.removeLast()
        processors.forEach { it.executor.invoke { fetchSize } }
        return last.executor.invoke {
            next()
            resultSet(this)
        }
    }

    open fun <T> firstOrNull(resultSet: ResultSet.() -> T): T? {
        if (isExecuted) {
            error("processors is already executed")
        }
        isExecuted = true
        val last = processors.removeLast()
        processors.forEach { it.executor.invoke { fetchSize } }
        return last.executor.invoke {
            if (next()) {
                resultSet(this)
            } else {
                null
            }
        }
    }

    open fun <T> map(resultSet: ResultSet.() -> T): List<T> {
        if (isExecuted) {
            error("processors is already executed")
        }
        isExecuted = true
        val last = processors.removeLast()
        processors.forEach { it.executor.invoke { fetchSize } }
        return last.executor.invoke {
            ArrayList<T>().also {
                while (next()) {
                    it += resultSet(this)
                }
            }
        }
    }

    open fun forEach(resultSet: ResultSet.() -> Unit) {
        if (isExecuted) {
            error("processors is already executed")
        }
        isExecuted = true
        val last = processors.removeLast()
        processors.forEach { it.executor.invoke { fetchSize } }
        last.executor.invoke {
            while (next()) {
                resultSet(this)
            }
        }
    }

    open fun forEachIndexed(resultSet: ResultSet.(index: Int) -> Unit) {
        if (isExecuted) {
            error("processors is already executed")
        }
        isExecuted = true
        val last = processors.removeLast()
        processors.forEach { it.executor.invoke { fetchSize } }
        last.executor.invoke {
            var i = 0
            while (next()) {
                resultSet(this, i++)
            }
        }
    }
}