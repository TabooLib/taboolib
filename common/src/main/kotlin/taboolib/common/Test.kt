package taboolib.common

import taboolib.common.platform.function.info
import java.lang.IllegalStateException

/**
 * TabooLib
 * taboolib.common.Test
 *
 * @author 坏黑
 * @since 2023/8/4 02:33
 */
abstract class Test {

    /**
     * 运行测试
     */
    abstract fun check(): List<Result>

    /**
     * 测试结果
     */
    sealed class Result(val reason: String)

    /**
     * 成功
     */
    class Success(reason: String) : Result(reason) {

        companion object {

            fun of(reason: String): Success {
                return Success(reason)
            }

            fun of(): Success {
                return Success("Unknown")
            }
        }
    }

    /**
     * 失败
     */
    class Failure(reason: String, val error: Throwable) : Result(reason) {

        companion object {

            fun of(reason: String, error: Throwable): Failure {
                return Failure(reason, error)
            }

            fun of(error: Throwable): Failure {
                return Failure(error.message ?: "NULL", error)
            }

            fun of(reason: String): Failure {
                return Failure(reason, RuntimeException("FAIL"))
            }

            fun of(): Failure {
                return Failure("Unknown", RuntimeException("FAIL"))
            }
        }
    }

    /**
     * 运行测试
     */
    protected fun sandbox(reason: String, func: () -> Unit): Result {
        return try {
            func()
            Success.of(reason)
        } catch (ex: Throwable) {
            Failure.of(reason, ex)
        }
    }

    companion object {

        /**
         * 批量测试，忽略异常
         */
        fun check(vararg tests: Test): BatchResult {
            return BatchResult(tests.flatMap { runCatching { it.check() }.getOrElse { ex -> listOf(Failure.of(ex)) } })
        }

        /**
         * 批量测试结果
         */
        data class BatchResult(val results: List<Result>) {

            val success = results.count { it is Success }
            val failure = results.count { it is Failure }

            /**
             * 打印测试结果
             */
            fun print(detail: Boolean = false) {
                val message = arrayListOf<String>()
                var len = results.maxOf { it.reason.length }
                results.forEach {
                    when (it) {
                        is Success -> message += "[ SUCCESS ] : ${it.reason}"
                        is Failure -> {
                            if (detail) {
                                message += "[ FAILURE ] : ${it.reason}"
                                it.error.printStackTrace()
                            } else {
                                message += "[ FAILURE ] : ${it.reason}${" ".repeat(len - it.reason.length)} : ${it.error.message ?: "NULL"}"
                            }
                        }
                    }
                }
                len = message.maxOf { it.length }
                val h1 = "=".repeat(len)
                val h2 = "-".repeat(len)
                info(h1)
                info("[ TOTAL : ${results.size}, SUCCESS : $success, FAILURE : $failure ]")
                info(h2)
                message.forEach { info(it) }
                info(h1)
            }
        }
    }
}