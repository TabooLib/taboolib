package io.izzel.taboolib.module.locale.logger;

import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 日志管理工具
 *
 * @author sky
 */
public class TLoggerManager {

    private static final Map<String, TLogger> map = new ConcurrentHashMap<>();

    public static TLogger getLogger(Plugin plugin) {
        return map.computeIfAbsent(plugin.getName(), n -> TLogger.getUnformatted(plugin));
    }
}
