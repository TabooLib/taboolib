package io.izzel.taboolib.plugin;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.*;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @Author 坏黑
 * @Since 2019-07-05 14:09
 */
public class InternalPluginLoader implements PluginLoader {

    private static JavaPluginLoader loader;

    public static PluginLoader getLoader() {
        return loader;
    }

    static {
        loader = new JavaPluginLoader(Bukkit.getServer());
    }

    @Override
    public Plugin loadPlugin(File file) throws UnknownDependencyException, InvalidPluginException {
        return loader.loadPlugin(file);
    }

    @Override
    public PluginDescriptionFile getPluginDescription(File file) throws InvalidDescriptionException {
        return loader.getPluginDescription(file);
    }

    @Override
    public Pattern[] getPluginFileFilters() {
        return loader.getPluginFileFilters();
    }

    @Override
    public Map<Class<? extends Event>, Set<RegisteredListener>> createRegisteredListeners(Listener listener, Plugin plugin) {
        return loader.createRegisteredListeners(listener, plugin);
    }

    @Override
    public void enablePlugin(Plugin plugin) {
        loader.enablePlugin(plugin);
    }

    @Override
    public void disablePlugin(Plugin plugin) {
        loader.disablePlugin(plugin);
    }
}
