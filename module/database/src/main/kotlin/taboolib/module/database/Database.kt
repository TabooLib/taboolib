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
    ),
    RuntimeDependency(
        "!com.mysql:mysql-connector-j:8.4.0",
        test = "!com.mysql.cj.jdbc.Driver",
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
        config.applyHost(host)
        return HikariDataSource(config)
    }

    /**
     * 创建一个 Hikari 配置
     */
    fun createHikariConfig(host: Host<*>): HikariConfig {
        val config = HikariConfig()
        config.applyHost(host)
        config.isAutoCommit = settingsFile.getBoolean("DefaultSettings.AutoCommit", true)
        config.minimumIdle = settingsFile.getInt("DefaultSettings.MinimumIdle", 1)
        config.maximumPoolSize = settingsFile.getInt("DefaultSettings.MaximumPoolSize", 10)
        config.validationTimeout = settingsFile.getLong("DefaultSettings.ValidationTimeout", 5000)
        config.connectionTimeout = settingsFile.getLong("DefaultSettings.ConnectionTimeout", 30000)
        config.idleTimeout = settingsFile.getLong("DefaultSettings.IdleTimeout", 600000)
        config.maxLifetime = settingsFile.getLong("DefaultSettings.MaxLifetime", 1800000)
        config.keepaliveTime = settingsFile.getLong("DefaultSettings.KeepaliveTime", 300000)
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

    private fun HikariConfig.applyHost(host: Host<*>) {
        when (host) {
            is HostSQL -> {
                jdbcUrl = host.connectionUrl
                username = host.user
                password = host.password
            }
            is HostPostgreSQL -> {
                jdbcUrl = host.connectionUrl
                username = host.user
                password = host.password
            }
            // SQLite 连接初始化：开启 WAL、busy_timeout、synchronous，降低多线程写冲突概率
            // WAL 让写不阻塞读，busy_timeout 让写冲突排队等待而非立刻 SQLITE_BUSY
            // SQLite 仍是文件级写锁，应用层仍应串行写库；这里只降低偶发冲突
            is HostSQLite -> {
                jdbcUrl = "${host.connectionUrl}?journal_mode=WAL&busy_timeout=5000&synchronous=NORMAL"
            }
            else -> error("Unsupported host: $host")
        }
        driverClassName = host.driverClass
    }
}
