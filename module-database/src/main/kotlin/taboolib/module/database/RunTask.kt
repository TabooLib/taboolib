package taboolib.module.database

import java.sql.ResultSet

/**
 * TabooLib
 * taboolib.module.database.RunTask
 *
 * @author sky
 * @since 2021/6/24 12:52 上午
 */
class RunTask(future: Future<ResultSet>) : QueryTask(future) {

    override fun run(): Int {
        return future.call { 0 }
    }

    override fun find(): Boolean {
        TODO("Not yet implemented")
    }

    override fun <T> first(resultSet: ResultSet.() -> T): T {
        TODO("Not yet implemented")
    }

    override fun <T> map(resultSet: ResultSet.() -> T): List<T> {
        TODO("Not yet implemented")
    }

    override fun forEach(resultSet: ResultSet.() -> Unit) {
        TODO("Not yet implemented")
    }
}