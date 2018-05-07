package com.ilummc.tlib.config;

import com.ilummc.tlib.annotations.TConfig;

/**
 * @author sky
 * @since 2018-04-22 14:31:11
 */
@TConfig(name = "tlib.yml", listenChanges = true)
public class TLibConfig {

    private boolean enablePlaceholderHookByDefault = false;

    public void setEnablePlaceholderHookByDefault(boolean enablePlaceholderHookByDefault) {
        this.enablePlaceholderHookByDefault = enablePlaceholderHookByDefault;
    }

    public boolean isEnablePlaceholderHookByDefault() {
        return enablePlaceholderHookByDefault;
    }
}
