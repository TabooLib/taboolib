package io.izzel.taboolib.module.db.source;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.izzel.taboolib.module.config.TConfig;
import io.izzel.taboolib.module.db.IHost;
import io.izzel.taboolib.module.db.sql.SQLHost;
import io.izzel.taboolib.module.db.sqlite.SQLiteHost;
import io.izzel.taboolib.module.inject.TInject;
import io.izzel.taboolib.module.locale.TLocale;
import org.bukkit.configuration.file.FileConfiguration;

import javax.sql.DataSource;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 连接池创建工具
 *
 * @Author sky
 * @Since 2018-05-16 21:59
 */
public class DBSource {

    @TInject(value = "datasource.yml", migrate = true)
    private static TConfig settings;
    private static final ConcurrentHashMap<IHost, DBSourceData> dataSource = new ConcurrentHashMap<>();

    public static DataSource create(IHost host) {
        return create(host, null);
    }

    public static DataSource create(IHost host, HikariConfig hikariConfig) {
        DBSourceData mapDataSource = dataSource.computeIfAbsent(host, x -> new DBSourceData(x, new HikariDataSource(hikariConfig == null ? createConfig(host) : hikariConfig)));
        mapDataSource.getActivePlugin().getAndIncrement();
        if (mapDataSource.getActivePlugin().get() == 1) {
            TLocale.Logger.info("MYSQL-HIKARI.CREATE-SUCCESS", host.getPlugin().getName(), host.getConnectionUrlSimple());
        } else {
            TLocale.Logger.info("MYSQL-HIKARI.CREATE-EXISTS", host.getPlugin().getName(), mapDataSource.getHost().getPlugin().getName());
        }
        return mapDataSource.getHikariDataSource();
    }

    public static void closeDataSourceForce() {
        dataSource.values().forEach(x -> x.getHikariDataSource().close());
    }

    public static void closeDataSource(IHost host) {
        if (host != null && dataSource.containsKey(host)) {
            DBSourceData mapDataSource = dataSource.get(host);
            if (mapDataSource.getActivePlugin().getAndDecrement() <= 1) {
                mapDataSource.getHikariDataSource().close();
                dataSource.remove(host);
                TLocale.Logger.info("MYSQL-HIKARI.CLOSE-SUCCESS", host.getPlugin().getName(), host.getConnectionUrlSimple());
            } else {
                TLocale.Logger.info("MYSQL-HIKARI.CLOSE-FAIL", host.getPlugin().getName(), String.valueOf(mapDataSource.getActivePlugin().get()));
            }
        }
    }

    public static HikariConfig createConfig(IHost host) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(host.getConnectionUrl());
        if (host instanceof SQLHost) {
            config.setDriverClassName(settings.getString("DefaultSettings.DriverClassName", "com.mysql.jdbc.Driver"));
            config.setUsername(((SQLHost) host).getUser());
            config.setPassword(((SQLHost) host).getPassword());
        } else if (host instanceof SQLiteHost) {
            config.setDriverClassName("org.sqlite.JDBC");
        } else {
            throw new IllegalArgumentException("Invalid host: " + host.getClass().getName());
        }
        config.setAutoCommit(settings.getBoolean("DefaultSettings.AutoCommit", true));
        config.setMinimumIdle(settings.getInt("DefaultSettings.MinimumIdle", 1));
        config.setMaximumPoolSize(settings.getInt("DefaultSettings.MaximumPoolSize", 10));
        config.setValidationTimeout(settings.getInt("DefaultSettings.ValidationTimeout", 5000));
        config.setConnectionTimeout(settings.getInt("DefaultSettings.ConnectionTimeout", 30000));
        config.setIdleTimeout(settings.getInt("DefaultSettings.IdleTimeout", 600000));
        config.setMaxLifetime(settings.getInt("DefaultSettings.MaxLifetime", 1800000));
        if (settings.contains("DefaultSettings.ConnectionTestQuery")) {
            config.setConnectionTestQuery(settings.getString("DefaultSettings.ConnectionTestQuery"));
        }
        if (settings.contains("DefaultSettings.DataSourceProperty")) {
            settings.getConfigurationSection("DefaultSettings.DataSourceProperty").getKeys(false).forEach(key -> config.addDataSourceProperty(key, settings.getString("DefaultSettings.DataSourceProperty." + key)));
        }
        return config;
    }

    public static ConcurrentHashMap<IHost, DBSourceData> getDataSource() {
        return dataSource;
    }

    public static FileConfiguration getSettings() {
        return settings;
    }
}
