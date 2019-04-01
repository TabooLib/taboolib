package com.ilummc.tlib.inject;

import com.ilummc.tlib.logger.TLogger;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public class TLoggerManager {

    private static final Map<Plugin, TLogger> map = new HashMap<>();

    public static void setDefaultLogger(Plugin plugin, TLogger logger) {
        map.put(plugin, logger);
    }

    public static TLogger getLogger(Plugin plugin) {
        TLogger logger = map.get(plugin);
        if (logger == null) {
            logger = TLogger.getUnformatted(plugin);
            map.put(plugin, logger);
        }
        return logger;
    }
}
