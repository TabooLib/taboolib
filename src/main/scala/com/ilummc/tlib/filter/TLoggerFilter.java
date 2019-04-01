package com.ilummc.tlib.filter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ilummc.tlib.filter.impl.FilterConfiguration;
import com.ilummc.tlib.filter.impl.FilterInvalidPluginLoader;
import me.skymc.taboolib.TabooLib;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * @author Bkm016
 * @since 2018-04-22
 */
public class TLoggerFilter implements Filter {

    private Filter filter;
    private Logger logger;
    private static List<TLoggerFilterHandler> handlers = Lists.newLinkedList();
    private static Map<String, TLoggerFilter> pluginFilter = Maps.newHashMap();
    private static TLoggerFilter globalFilter;
    private static String playerConnectionName;

    static {
        handlers.add(new FilterConfiguration());
        handlers.add(new FilterInvalidPluginLoader());
        //        handlers.add(new FilterExceptionMirror());
    }

    public static void preInit() {
        inject(new TLoggerFilter(), Bukkit.getLogger());
        inject(new TLoggerFilter(), TabooLib.instance().getLogger());
        try {
            playerConnectionName = Class.forName("net.minecraft.server." + TabooLib.getVersion() + ".PlayerConnection").getName();
        } catch (Exception ignored) {
        }
    }

    public static void postInit() {
        Arrays.stream(Bukkit.getPluginManager().getPlugins()).filter(TabooLib::isDependTabooLib).forEach(plugin -> inject(new TLoggerFilter(), plugin.getLogger()));
    }

    public static void inject0() {
        inject(new TLoggerFilter(), Logger.getLogger(playerConnectionName));
    }

    public static void inject(TLoggerFilter filter, Logger logger) {
        if (!(logger.getFilter() instanceof TLoggerFilter)) {
            try {
                filter.filter = logger.getFilter();
                filter.logger = logger;
                logger.setFilter(filter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void eject(Plugin plugin) {
        try {
            if (plugin.getLogger().getFilter() instanceof TLoggerFilter) {
                ((TLoggerFilter) plugin.getLogger().getFilter()).filter = null;
                ((TLoggerFilter) plugin.getLogger().getFilter()).logger = null;
                plugin.getLogger().setFilter(null);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static TLoggerFilter getGlobalFilter() {
        return globalFilter;
    }

    public static Map<String, TLoggerFilter> getPluginFilter() {
        return pluginFilter;
    }

    public static List<TLoggerFilterHandler> getHandlers() {
        return handlers;
    }

    public Filter getFilter() {
        return filter;
    }

    public Logger getLogger() {
        return logger;
    }

    @Override
    public boolean isLoggable(LogRecord e) {
        return handlers.stream().allMatch(filter -> filter.isLoggable(e)) && (filter == null || filter.isLoggable(e));
    }
}
