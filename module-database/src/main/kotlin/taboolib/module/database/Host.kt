package taboolib.module.database

import javax.sql.DataSource

/**
 * @author sky
 * @since 2018-05-14 19:07
 */
abstract class Host<T : ColumnBuilder> {

    abstract val columnBuilder: ColumnBuilder

    abstract val connectionUrl: String?

    abstract val connectionUrlSimple: String?

    fun createDataSource(): DataSource {
        return Database.createDataSource(this)
    }
}