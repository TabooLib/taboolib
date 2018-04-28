package com.ilummc.tlib.config;

import com.ilummc.tlib.annotations.Config;

/**
 * @author sky
 * @since 2018-04-22 14:31:11
 */
@Config(name = "tlib.yml", listenChanges = true, readOnly = false)
public class TLibConfig {

    private boolean enablePlaceholderHookByDefault = false;

    public boolean isEnablePlaceholderHookByDefault() {
        return enablePlaceholderHookByDefault;
    }
}
