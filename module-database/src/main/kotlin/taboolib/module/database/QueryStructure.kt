package taboolib.module.database

import java.sql.Connection
import java.sql.PreparedStatement

/**
 * TabooLib
 * taboolib.module.database.QueryStructure
 *
 * @author sky
 * @since 2021/6/23 3:03 下午
 */
class QueryStructure {

    var statement: (PreparedStatement.() -> Unit)? = null
    var statementPost: (PreparedStatement.() -> Unit)? = null
    var connectionPost: (Connection.() -> Unit)? = null
}