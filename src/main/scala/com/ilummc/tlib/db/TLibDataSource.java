package com.ilummc.tlib.db;

import com.ilummc.tlib.TLib;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.javalite.activejdbc.Base;

import javax.sql.DataSource;
import java.util.Properties;

public class TLibDataSource {

    private final HikariDataSource dataSource;

    TLibDataSource() {
        Properties properties = new Properties();
        properties.put("jdbcUrl", TLib.getTLib().getConfig().getJdbcUrl());
        properties.put("username", TLib.getTLib().getConfig().getUsername());
        properties.put("password", TLib.getTLib().getConfig().getPassword());
        properties.put("dataSourceClassName", TLib.getTLib().getConfig().getDataSourceClassName());
        properties.put("driverClassName", TLib.getTLib().getConfig().getDriverClassName());
        TLib.getTLib().getConfig().getSettings().forEach((k, v) -> properties.put("dataSource." + k, v));
        dataSource = new HikariDataSource(new HikariConfig(properties));
        Base.open(dataSource);
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void disconnect() {
        Base.close();
    }

}
