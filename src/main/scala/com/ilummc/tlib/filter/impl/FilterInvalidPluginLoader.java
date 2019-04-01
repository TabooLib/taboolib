package com.ilummc.tlib.filter.impl;

import com.ilummc.tlib.filter.TLoggerFilterHandler;

import java.util.logging.LogRecord;

/**
 * @Author 坏黑
 * @Since 2018-11-29 11:47
 */
public class FilterInvalidPluginLoader extends TLoggerFilterHandler {

    @Override
    public boolean isLoggable(LogRecord e) {
        // 屏蔽插件加载器注入导致的警告信息
        return !String.valueOf(e.getMessage()).contains("Enabled plugin with unregistered PluginClassLoader");
    }
}
