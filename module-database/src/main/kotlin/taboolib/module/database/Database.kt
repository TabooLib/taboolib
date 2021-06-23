package taboolib.module.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import taboolib.common.env.RuntimeDependency
import taboolib.common.platform.releaseResourceFile
import taboolib.module.configuration.SecuredFile
import javax.sql.DataSource

@RuntimeDependency("com.zaxxer:HikariCP:4.0.3", test = "com.zaxxer.hikari.HikariDataSource")
object Database {

    var settingsPath = "datasource.yml"

    val settingsFile by lazy {
        SecuredFile.loadConfiguration(releaseResourceFile(settingsPath))
    }

    fun createDataSource(host: Host, hikariConfig: HikariConfig? = null): DataSource {
        return HikariDataSource(hikariConfig ?: createHikariConfig(host))
    }

    fun createHikariConfig(host: Host): HikariConfig {
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
            settingsFile.getConfigurationSection("DefaultSettings.DataSourceProperty").getKeys(false).forEach { key ->
                config.addDataSourceProperty(key, settingsFile.getString("DefaultSettings.DataSourceProperty.$key"))
            }
        }
        return config
    }
}