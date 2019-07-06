package io.izzel.taboolib.module.db.source;

import com.zaxxer.hikari.HikariDataSource;
import io.izzel.taboolib.module.db.IHost;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author sky
 * @Since 2018-05-17 23:47
 */
public class DBSourceData {

    private IHost host;
    private AtomicInteger activePlugin;
    private HikariDataSource hikariDataSource;

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
