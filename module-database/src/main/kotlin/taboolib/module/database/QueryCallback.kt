package taboolib.module.database

import java.sql.Connection
import java.sql.PreparedStatement

/**
 * TabooLib
 * taboolib.module.database.QueryCallback
 *
 * @author sky
 * @since 2021/6/23 10:31 下午
 */
open class QueryCallback {

    internal var finishFunc: (PreparedStatement.(Connection) -> Unit)? = null

    fun finally(func: PreparedStatement.(Connection) -> Unit) {
        finishFunc = func
    }
}