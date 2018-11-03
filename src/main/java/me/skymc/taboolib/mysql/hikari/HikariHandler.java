package me.skymc.taboolib.mysql.hikari;

import com.google.common.base.Preconditions;
import com.ilummc.tlib.resources.TLocale;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.skymc.taboolib.Main;
import me.skymc.taboolib.fileutils.ConfigUtils;
import me.skymc.taboolib.mysql.builder.SQLHost;
import org.bukkit.configuration.file.FileConfiguration;

import javax.sql.DataSource;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author sky
 * @Since 2018-05-16 21:59
 */
public class HikariHandler {

    private static ConcurrentHashMap<SQLHost, MapDataSource> dataSource = new ConcurrentHashMap<>();
    private static FileConfiguration settings;

    public static void init() {
        settings = ConfigUtils.saveDefaultConfig(Main.getInst(), "hikarisettings.yml");
    }

    public static DataSource createDataSource(SQLHost host) {
        return createDataSource(host, null);
    }

    public static HikariDataSource createDataSource(SQLHost host, HikariConfig hikariConfig) {
        MapDataSource mapDataSource = dataSource.computeIfAbsent(host, x -> new MapDataSource(x, new HikariDataSource(hikariConfig == null ? createConfig(host) : hikariConfig)));
        mapDataSource.getActivePlugin().getAndIncrement();
        if (mapDataSource.getActivePlugin().get() == 1) {
            TLocale.Logger.info("MYSQL-HIKARI.CREATE-SUCCESS", host.getPlugin().getName(), host.getConnectionUrlSimple());
        } else {
            TLocale.Logger.info("MYSQL-HIKARI.CREATE-EXISTS", host.getPlugin().getName(), mapDataSource.getSqlHost().getPlugin().getName());
        }
        return mapDataSource.getHikariDataSource();
    }

    public static void closeDataSourceForce() {
        Preconditions.checkArgument(Main.isDisable(), "Cannot be invoked when the server is running.");
        dataSource.values().forEach(x -> x.getHikariDataSource().close());
    }

    public static void closeDataSource(SQLHost host) {
        if (host != null && dataSource.containsKey(host)) {
            MapDataSource mapDataSource = dataSource.get(host);
            if (mapDataSource.getActivePlugin().getAndDecrement() <= 1) {
                mapDataSource.getHikariDataSource().close();
                dataSource.remove(host);
                TLocale.Logger.info("MYSQL-HIKARI.CLOSE-SUCCESS", host.getPlugin().getName(), host.getConnectionUrlSimple());
            } else {
                TLocale.Logger.info("MYSQL-HIKARI.CLOSE-FAIL", host.getPlugin().getName(), String.valueOf(mapDataSource.getActivePlugin().get()));
            }
        }
    }

    public static HikariConfig createConfig(SQLHost sqlHost) {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(settings.getString("DefaultSettings.DriverClassName", "com.mysql.jdbc.Driver"));
        config.setJdbcUrl(sqlHost.getConnectionUrl());
        config.setUsername(sqlHost.getUser());
        config.setPassword(sqlHost.getPassword());
        config.setAutoCommit(settings.getBoolean("DefaultSettings.AutoCommit", true));
        config.setMinimumIdle(settings.getInt("DefaultSettings.MinimumIdle", -1));
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

    // *********************************
    //
    //        Getter and Setter
    //
    // *********************************

    public static ConcurrentHashMap<SQLHost, MapDataSource> getDataSource() {
        return dataSource;
    }

    public static FileConfiguration getSettings() {
        return settings;
    }
}
