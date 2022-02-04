package taboolib.module.database

import com.zaxxer.hikari.HikariDataSource
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import java.util.concurrent.CopyOnWriteArrayList
import javax.sql.DataSource

/**
 * @author sky
 * @since 2018-05-14 19:07
 */
abstract class Host<T : ColumnBuilder> {

    abstract val columnBuilder: ColumnBuilder

    abstract val connectionUrl: String?

    abstract val connectionUrlSimple: String?

    fun createDataSource(autoRelease: Boolean = true): DataSource {
        return Database.createDataSource(this).also {
            if (autoRelease) {
                dataSources += it as HikariDataSource
            }
        }
    }

    companion object {

        internal val dataSources = CopyOnWriteArrayList<HikariDataSource>()

        @Awake(LifeCycle.DISABLE)
        internal fun release() {
            dataSources.forEach { it.close() }
        }
    }
}