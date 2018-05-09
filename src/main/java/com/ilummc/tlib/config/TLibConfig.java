package com.ilummc.tlib.config;

import com.ilummc.tlib.annotations.TConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * @author sky
 * @since 2018-04-22 14:31:11
 */
@TConfig(name = "tlib.yml", listenChanges = true)
public class TLibConfig {

    private String dataSourceClassName;

    private String jdbcUrl = "jdbc:h2:file:~/plugins/TabooLib/h2";

    private String driverClassName;

    private String username = "";

    public String getDataSourceClassName() {
        return dataSourceClassName;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public Map<String, Object> getSettings() {
        return settings;
    }

    private String password = "";

    private int maximumPoolSize = 4;

    private Map<String, Object> settings = new HashMap<String, Object>() {{
        put("cachePrepStmts", true);
        put("useServerPrepStmts", true);
    }};

}
