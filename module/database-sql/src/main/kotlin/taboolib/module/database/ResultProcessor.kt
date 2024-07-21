package taboolib.module.database

import java.sql.ResultSet
import java.util.function.Supplier

/**
 * 结果处理器
 *
 * @author sky
 * @since 2021/6/23 2:11 下午
 */
open class ResultProcessor(val query: String, val executor: Executable<ResultSet>) {

    /** 仅运行 */
    class Update(query: String, val callback: Supplier<Int>) : ResultProcessor(query, Executable.Empty) {

        override fun run(): Int {
            return callback.get()
        }
    }

    /** 是否已经执行 */
    var isExecuted = false
        private set

    /** 运行并返回结果 */
    open fun run(): Int {
        return if (!isExecuted) {
            isExecuted = true
            executor.invoke { fetchSize }
        } else {
            0
        }
    }

    /** 运行并返回是否有结果 */
    open fun find(): Boolean {
        if (isExecuted) {
            error("processor is already executed: $query")
        }
        isExecuted = true
        return executor.invoke { next() }
    }

    /** 运行并返回第一个结果 */
    open fun <T> first(call: ResultSet.() -> T): T {
        if (isExecuted) {
            error("processor is already executed: $query")
        }
        isExecuted = true
        return executor.invoke {
            next()
            call(this)
        }
    }

    /** 运行并返回第一个结果，如果没有结果则返回 null */
    open fun <T> firstOrNull(call: ResultSet.() -> T): T? {
        if (isExecuted) {
            error("processor is already executed: $query")
        }
        isExecuted = true
        return executor.invoke {
            if (next()) {
                call(this)
            } else {
                null
            }
        }
    }

    /** 运行并返回所有结果 */
    open fun <T> map(call: ResultSet.() -> T): List<T> {
        if (isExecuted) {
            error("processor is already executed: $query")
        }
        isExecuted = true
        return executor.invoke {
            val arr = arrayListOf<T>()
            while (next()) {
                arr += call(this)
            }
            arr
        }
    }

    /** 运行并返回所有结果，如果结果为 null 则不添加 */
    open fun <T> mapNotNull(call: ResultSet.() -> T?): List<T> {
        if (isExecuted) {
            error("processor is already executed: $query")
        }
        isExecuted = true
        return executor.invoke {
            val arr = arrayListOf<T>()
            while (next()) {
                call(this)?.also { t -> arr += t }
            }
            arr
        }
    }

    /** 运行并遍历所有结果 */
    open fun forEach(call: ResultSet.() -> Unit) {
        if (isExecuted) {
            error("processor is already executed: $query")
        }
        isExecuted = true
        executor.invoke {
            while (next()) {
                call(this)
            }
        }
    }

    /** 运行并遍历所有结果 */
    open fun forEachIndexed(call: ResultSet.(index: Int) -> Unit) {
        if (isExecuted) {
            error("processor is already executed: $query")
        }
        isExecuted = true
        executor.invoke {
            var i = 0
            while (next()) {
                call(this, i++)
            }
        }
    }
}

/** 向下兼容 */
@Deprecated("Use Processor instead.", ReplaceWith("Processor"))
typealias QueryTask = ResultProcessor

/** 向下兼容 */
@Deprecated("Use ProcessorList instead.", ReplaceWith("ProcessorList"))
typealias QueryTasks = ResultProcessorList