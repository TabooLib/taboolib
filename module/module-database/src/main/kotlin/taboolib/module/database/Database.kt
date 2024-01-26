package taboolib.module.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import taboolib.common.Inject
import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import javax.sql.DataSource

@Inject
@RuntimeDependencies(
    RuntimeDependency(
        "!org.slf4j:slf4j-api:2.0.8",
        test = "!org.slf4j_2_0_8.LoggerFactory",
        relocate = ["!org.slf4j", "!org.slf4j_2_0_8"],
        transitive = false
    ),
    RuntimeDependency(
        "!com.zaxxer:HikariCP:4.0.3",
        test = "!com.zaxxer.hikari_4_0_3.HikariDataSource",
        relocate = ["!com.zaxxer.hikari", "!com.zaxxer.hikari_4_0_3", "!org.slf4j", "!org.slf4j_2_0_8"],
        transitive = false
    )
)
object Database {

    @Config("datasource.yml")
    lateinit var settingsFile: Configuration

    /**
     * 创建一个关闭数据库连接的回调函数
     */
    fun prepareClose(func: Runnable) {
        Host.callbackClose += func
    }

    /**
     * 创建一个数据库连接池
     */
    fun createDataSource(host: Host<*>, hikariConfig: HikariConfig? = null): DataSource {
        return HikariDataSource(hikariConfig ?: createHikariConfig(host))
    }

    /**
     * 不使用配置文件创建一个数据库连接池
     */
    fun createDataSourceWithoutConfig(host: Host<*>): DataSource {
        val config = HikariConfig()
        config.jdbcUrl = host.connectionUrl
        if (host is HostSQL) {
            config.username = host.user
            config.password = host.password
        } else {
            error("Unsupported host: $host")
        }
        return HikariDataSource(config)
    }

    /**
     * 创建一个 Hikari 配置
     */
    fun createHikariConfig(host: Host<*>): HikariConfig {
        val config = HikariConfig()
        config.jdbcUrl = host.connectionUrl
        when (host) {
            is HostSQL -> {
                config.driverClassName = settingsFile.getString("DefaultSettings.DriverClassName", "com.mysql.jdbc.Driver")
                config.username = host.user
                config.password = host.password
            }
            is HostSQLite -> {
                config.driverClassName = "org.sqlite.JDBC"
            }
            else -> {
                error("Unsupported host: $host")
            }
        }
        config.isAutoCommit = settingsFile.getBoolean("DefaultSettings.AutoCommit", true)
        config.minimumIdle = settingsFile.getInt("DefaultSettings.MinimumIdle", 1)
        config.maximumPoolSize = settingsFile.getInt("DefaultSettings.MaximumPoolSize", 10)
        config.validationTimeout = settingsFile.getLong("DefaultSettings.ValidationTimeout", 5000)
        config.connectionTimeout = settingsFile.getLong("DefaultSettings.ConnectionTimeout", 30000)
        config.idleTimeout = settingsFile.getLong("DefaultSettings.IdleTimeout", 600000)
        config.maxLifetime = settingsFile.getLong("DefaultSettings.MaxLifetime", 1800000)
        if (settingsFile.contains("DefaultSettings.ConnectionTestQuery")) {
            config.connectionTestQuery = settingsFile.getString("DefaultSettings.ConnectionTestQuery")
        }
        if (settingsFile.contains("DefaultSettings.DataSourceProperty")) {
            settingsFile.getConfigurationSection("DefaultSettings.DataSourceProperty")?.getKeys(false)?.forEach { key ->
                config.addDataSourceProperty(key, settingsFile.getString("DefaultSettings.DataSourceProperty.$key"))
            }
        }
        return config
    }
}