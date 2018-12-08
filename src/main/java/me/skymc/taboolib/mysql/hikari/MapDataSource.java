package me.skymc.taboolib.mysql.hikari;

import com.zaxxer.hikari.HikariDataSource;
import me.skymc.taboolib.mysql.IHost;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author sky
 * @Since 2018-05-17 23:47
 */
public class MapDataSource {

    private IHost host;
    private AtomicInteger activePlugin;
    private HikariDataSource hikariDataSource;

    MapDataSource(IHost host, HikariDataSource hikariDataSource) {
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
