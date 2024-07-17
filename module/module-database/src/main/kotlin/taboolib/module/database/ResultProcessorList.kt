package taboolib.module.database

import java.sql.ResultSet

/**
 * 多结果处理器
 *
 * @author sky
 * @since 2021/6/23 2:11 下午
 */
open class ResultProcessorList(processors: List<ResultProcessor>, val source: ExecutableSource? = null) {

    private val processors = processors.toMutableList()

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
        return try {
            processors.forEach { it.run() }
            last.run()
        } finally {
            source?.close()
        }
    }

    open fun find(): Boolean {
        if (isExecuted) {
            error("processors is already executed")
        }
        isExecuted = true
        val last = processors.removeLast()
        return try {
            processors.forEach { it.run() }
            last.executor.invoke { next() }
        } finally {
            source?.close()
        }
    }

    open fun <T> first(resultSet: ResultSet.() -> T): T {
        if (isExecuted) {
            error("processors is already executed")
        }
        isExecuted = true
        val last = processors.removeLast()
        return try {
            processors.forEach { it.run() }
            last.executor.invoke {
                next()
                resultSet(this)
            }
        } finally {
            source?.close()
        }
    }

    open fun <T> firstOrNull(resultSet: ResultSet.() -> T): T? {
        if (isExecuted) {
            error("processors is already executed")
        }
        isExecuted = true
        val last = processors.removeLast()
        return try {
            processors.forEach { it.run() }
            last.executor.invoke {
                if (next()) {
                    resultSet(this)
                } else {
                    null
                }
            }
        } finally {
            source?.close()
        }
    }

    open fun <T> map(resultSet: ResultSet.() -> T): List<T> {
        if (isExecuted) {
            error("processors is already executed")
        }
        isExecuted = true
        val last = processors.removeLast()
        return try {
            processors.forEach { it.run() }
            last.executor.invoke {
                val arr = ArrayList<T>()
                while (next()) {
                    arr += resultSet(this)
                }
                arr
            }
        } finally {
            source?.close()
        }
    }

    open fun forEach(resultSet: ResultSet.() -> Unit) {
        if (isExecuted) {
            error("processors is already executed")
        }
        isExecuted = true
        val last = processors.removeLast()
        try {
            processors.forEach { it.run() }
            last.executor.invoke {
                while (next()) {
                    resultSet(this)
                }
            }
        } finally {
            source?.close()
        }
    }

    open fun forEachIndexed(resultSet: ResultSet.(index: Int) -> Unit) {
        if (isExecuted) {
            error("processors is already executed")
        }
        isExecuted = true
        val last = processors.removeLast()
        try {
            processors.forEach { it.run() }
            last.executor.invoke {
                var i = 0
                while (next()) {
                    resultSet(this, i++)
                }
            }
        } finally {
            source?.close()
        }
    }
}