package me.skymc.taboolib.mysql.hikari;

import com.google.common.base.Preconditions;
import com.ilummc.tlib.resources.TLocale;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.skymc.taboolib.Main;
import me.skymc.taboolib.mysql.builder.SQLHost;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author sky
 * @Since 2018-05-16 21:59
 */
public class HikariHandler {

    private static ConcurrentHashMap<SQLHost, MapDataSource> dataSource = new ConcurrentHashMap<>();

    /**
     * 根据数据库地址创建连接池，如果已经存在则返回引用
     *
     * @param host 数据库地址
     * @return {@link HikariDataSource}
     * @throws java.sql.SQLException 数据库连接失败异常
     */
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

    /**
     * 强制注销所有已注册的连接池
     * 只能在 TabooLib 卸载后调用
     */
    public static void closeDataSourceForce() {
        Preconditions.checkArgument(Main.isDisable(), "Cannot be invoked when the server is running.");
        dataSource.values().forEach(x -> x.getHikariDataSource().close());
    }

    /**
     * 注销连接池
     * 如果连接池有 1 个以上的插件正在使用则跳过，反之则注销并从缓存中移除
     *
     * @param host 地址
     */
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

    /**
     * 根据数据库地址创建 HikariConfig 对象
     *
     * @param sqlHost 数据库地址
     * @return {@link HikariConfig}
     */
    public static HikariConfig createConfig(SQLHost sqlHost) {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("com.mysql.jdbc.Driver");
        config.setJdbcUrl(sqlHost.getConnectionUrl());
        config.setUsername(sqlHost.getUser());
        config.setPassword(sqlHost.getPassword());
        config.setConnectionTestQuery("SELECT 1");
        config.setAutoCommit(true);
        config.setMinimumIdle(1);
        config.setMaximumPoolSize(10);
        // 用来指定验证连接有效性的超时时间（毫秒/默认: 5秒）
        config.setValidationTimeout(3000);
        // 等待连接池分配连接的最大时长（毫秒/默认: 30秒），超过这个时长还没可用的连接则发生 SQLException
        config.setConnectionTimeout(10000);
        // 一个连接idle状态的最大时长（毫秒/默认: 10分钟），超时则被释放
        config.setIdleTimeout(60000);
        // 一个连接的生命时长（毫秒/默认: 30分钟），超时而且没被使用则被释放
        config.setMaxLifetime(60000);
        // 是否自定义配置，为true时下面两个参数才生效
        config.addDataSourceProperty("cachePrepStmts", "true");
        // 连接池大小默认25，官方推荐250-500
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        // 单条语句最大长度默认256，官方推荐2048
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        // 新版本MySQL支持服务器端准备，开启能够得到显著性能提升
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("useLocalTransactionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        return config;
    }
}
