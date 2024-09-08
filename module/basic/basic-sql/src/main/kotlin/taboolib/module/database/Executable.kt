package taboolib.module.database

import java.sql.ResultSet

/**
 * 回调函数接口
 *
 * @author sky
 * @since 2021/6/24 1:58 上午
 */
interface Executable<T> {

    /** 空回调 */
    object Empty : Executable<ResultSet> {

        override fun <C> invoke(func: ResultSet.() -> C): C {
            error("Unsupported")
        }
    }

    /** 执行函数 */
    fun <C> invoke(func: T.() -> C): C
}