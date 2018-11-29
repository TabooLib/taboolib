package com.ilummc.tlib.filter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ilummc.tlib.filter.impl.FilterConfiguration;
import com.ilummc.tlib.filter.impl.FilterExceptionMirror;
import com.ilummc.tlib.filter.impl.FilterInvalidPluginLoader;
import me.skymc.taboolib.TabooLib;
import org.bukkit.Bukkit;

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
    private static List<TLoggerFilterHandler> handlers = Lists.newLinkedList();
    private static Map<String, TLoggerFilter> pluginFilter = Maps.newHashMap();
    private static TLoggerFilter globalFilter;

    static {
        handlers.add(new FilterConfiguration());
        handlers.add(new FilterExceptionMirror());
        handlers.add(new FilterInvalidPluginLoader());
    }

    public static void preInit() {
        inject(new TLoggerFilter(), Bukkit.getLogger());
        inject(new TLoggerFilter(), TabooLib.instance().getLogger());
    }

    public static void postInit() {
        Arrays.stream(Bukkit.getPluginManager().getPlugins()).filter(TabooLib::isDependTabooLib).forEach(plugin -> inject(new TLoggerFilter(), plugin.getLogger()));
    }

    public static void inject(TLoggerFilter filter, Logger logger) {
        try {
            filter.filter = logger.getFilter();
            logger.setFilter(filter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Filter getFilter() {
        return filter;
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

    @Override
    public boolean isLoggable(LogRecord e) {
        return handlers.stream().allMatch(filter -> filter.isLoggable(e)) && (filter == null || filter.isLoggable(e));
    }
}
