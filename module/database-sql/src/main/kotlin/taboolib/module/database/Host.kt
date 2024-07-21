package taboolib.module.database

import com.zaxxer.hikari.HikariDataSource
import taboolib.common.Inject
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import java.util.concurrent.CopyOnWriteArrayList
import javax.sql.DataSource

/**
 * 数据库地址
 *
 * @author sky
 * @since 2018-05-14 19:07
 */
abstract class Host<T : ColumnBuilder> {

    abstract val columnBuilder: ColumnBuilder

    abstract val connectionUrl: String?

    abstract val connectionUrlSimple: String?

    fun createDataSource(autoRelease: Boolean = true, withoutConfig: Boolean = false): DataSource {
        val dataSource = if (withoutConfig) Database.createDataSourceWithoutConfig(this) else Database.createDataSource(this)
        return dataSource.also {
            if (autoRelease) {
                dataSources += it as HikariDataSource
            }
        }
    }

    @Inject
    internal companion object {

        internal val dataSources = CopyOnWriteArrayList<HikariDataSource>()
        internal val callbackClose = CopyOnWriteArrayList<Runnable>()

        @Awake(LifeCycle.DISABLE)
        internal fun release() {
            callbackClose.forEach { it.run() }
            dataSources.forEach { it.close() }
        }
    }
}