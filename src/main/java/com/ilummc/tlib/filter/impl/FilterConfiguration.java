package com.ilummc.tlib.filter.impl;

import com.ilummc.tlib.filter.TLoggerFilterHandler;

import java.util.Arrays;
import java.util.logging.LogRecord;

/**
 * @Author 坏黑
 * @Since 2018-11-29 11:47
 */
public class FilterConfiguration extends TLoggerFilterHandler {

    @Override
    public boolean isLoggable(LogRecord e) {
        if (String.valueOf(e.getMessage()).contains("Cannot load configuration from stream")) {
            StackTraceElement[] elements = Thread.currentThread().getStackTrace();
            for (StackTraceElement element : elements) {
                if (element.getClassName().contains("ConfigUtils")) {
                    // Bukkit 拦截异常？我再扔一个
                    System.out.println(Arrays.asList(e.getParameters()));
                }
            }
            return false;
        }
        return true;
    }
}
