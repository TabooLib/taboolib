package com.ilummc.tlib.config;

import com.ilummc.tlib.annotations.TConfig;

/**
 * @author sky
 * @since 2018-04-22 14:31:11
 */
@TConfig(name = "tlib.yml", listenChanges = true)
public class TLibConfig {

    @Getter
    private String dataSourceClassName;

    @Getter
    private String jdbcUrl = "jdbc:h2:file:~/plugins/TabooLib/h2";

    @Getter
    private String driverClassName;

    @Getter
    private String username = "";

    @Getter
    private String password = "";

    @Getter
    private int maximumPoolSize = 4;

    @Getter
    private Map<String, Object> settings = new HashMap<String, Object>() {{
        put("cachePrepStmts", true);
        put("useServerPrepStmts", true);
    }};

}
