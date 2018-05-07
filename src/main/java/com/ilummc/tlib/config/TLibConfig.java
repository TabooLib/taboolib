package com.ilummc.tlib.config;

import com.ilummc.tlib.annotations.Config;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author sky
 * @since 2018-04-22 14:31:11
 */
@Config(name = "tlib.yml", listenChanges = true, readOnly = false)
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
