package com.ilummc.tlib.filter;

import org.bukkit.Bukkit;

import java.util.Arrays;
import java.util.logging.Filter;
import java.util.logging.LogRecord;

/**
 * @author Bkm016
 * @since 2018-04-22
 */
public class TLoggerFilter implements Filter {

    public static void init() {
        Bukkit.getLogger().setFilter(new TLoggerFilter());
    }

    @Override
    public boolean isLoggable(LogRecord e) {
        if (e.getMessage().contains("Cannot load configuration from stream")) {
            StackTraceElement[] elements = Thread.currentThread().getStackTrace();
            for (StackTraceElement element : elements) {
                if (element.getClassName().contains("ConfigUtils")) {
                    System.out.println(Arrays.asList(e.getParameters()));
                }
            }
            return false;
        } else {
            return !e.getMessage().contains("Enabled plugin with unregistered PluginClassLoader");
        }
    }
}
