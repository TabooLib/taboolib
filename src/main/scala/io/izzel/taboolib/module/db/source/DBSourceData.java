package io.izzel.taboolib.module.db.source;

import com.zaxxer.hikari.HikariDataSource;
import io.izzel.taboolib.module.db.IHost;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 连接池缓存
 *
 * @author sky
 * @since 2018-05-17 23:47
 */
public class DBSourceData {

    private final IHost host;
    private final AtomicInteger activePlugin;
    private final HikariDataSource hikariDataSource;

    DBSourceData(IHost host, HikariDataSource hikariDataSource) {
        this.host = host;
        this.activePlugin = new AtomicInteger();
        this.hikariDataSource = hikariDataSource;
    }

    public IHost getHost() {
        return host;
    }

    public AtomicInteger getActivePlugin() {
        return activePlugin;
    }

    public HikariDataSource getHikariDataSource() {
        return hikariDataSource;
    }
}
