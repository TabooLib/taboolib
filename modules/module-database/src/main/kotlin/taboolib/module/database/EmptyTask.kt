package taboolib.module.database

import java.sql.ResultSet

object EmptyTask : QueryTask(EmptyFuture) {

    override fun run(): Int {
        return 0
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