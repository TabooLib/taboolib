package taboolib.module.database

import java.sql.ResultSet

object EmptyFuture : Future<ResultSet> {

    override fun <C> call(func: ResultSet.() -> C): C {
        TODO("Not yet implemented")
    }
}