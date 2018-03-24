package me.skymc.taboolib.plugin;

import com.google.common.base.Joiner;
import me.skymc.taboolib.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.Event;
import org.bukkit.plugin.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PluginUtils
{
	public static String consolidateStrings(final String[] args, final int start) {
        StringBuilder ret = new StringBuilder(args[start]);
        if (args.length > start + 1) {
            for (int i = start + 1; i < args.length; ++i) {
                ret.append(" ").append(args[i]);
            }
        }
        return ret.toString();
    }
	
    public static void enable(final Plugin plugin) {
        if (plugin != null && !plugin.isEnabled()) {
            Bukkit.getPluginManager().enablePlugin(plugin);
        }
    }
    
    public static void enableAll() {
        for (final Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (!isIgnored(plugin)) {
                enable(plugin);
            }
        }
    }
    
    public static void disable(final Plugin plugin) {
        if (plugin != null && plugin.isEnabled()) {
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }
    
    public static void disableAll() {
        for (final Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (!isIgnored(plugin)) {
                disable(plugin);
            }
        }
    }
    
    public static String getFormattedName(final Plugin plugin) {
        return getFormattedName(plugin, false);
    }
    
    public static String getFormattedName(final Plugin plugin, final boolean includeVersions) {
        final ChatColor color = plugin.isEnabled() ? ChatColor.GREEN : ChatColor.RED;
        String pluginName = color + plugin.getName();
        if (includeVersions) {
            pluginName = pluginName + " (" + plugin.getDescription().getVersion() + ")";
        }
        return pluginName;
    }
    
    public static Plugin getPluginByName(final String[] args, final int start) {
        return getPluginByName(consolidateStrings(args, start));
    }
    
    public static Plugin getPluginByName(final String name) {
        for (final Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (name.equalsIgnoreCase(plugin.getName())) {
                return plugin;
            }
        }
        return null;
    }
    
    public static List<String> getPluginNames(final boolean fullName) {
        final List<String> plugins = new ArrayList<>();
        for (final Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            plugins.add(fullName ? plugin.getDescription().getFullName() : plugin.getName());
        }
        return plugins;
    }
    
    public static String getPluginVersion(final String name) {
        final Plugin plugin = getPluginByName(name);
        if (plugin != null && plugin.getDescription() != null) {
            return plugin.getDescription().getVersion();
        }
        return null;
    }
    
    public static String getUsages(final Plugin plugin) {
        final List<String> parsedCommands = new ArrayList<>();
        final Map<String, Map<String, Object>> commands = plugin.getDescription().getCommands();
        if (commands != null) {
            for (final Entry<String, Map<String, Object>> thisEntry : commands.entrySet()) {
                if (thisEntry != null) {
                    parsedCommands.add(thisEntry.getKey());
                }
            }
        }
        if (parsedCommands.isEmpty()) {
            return "No commands registered.";
        }
        return Joiner.on(", ").join(parsedCommands);
    }
    
    @SuppressWarnings("unchecked")
	public static List<String> findByCommand(final String command) {
        final List<String> plugins = new ArrayList<>();
        for (final Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            final Map<String, Map<String, Object>> commands = plugin.getDescription().getCommands();
            if (commands != null) {
                for (final Map.Entry<String, Map<String, Object>> commandNext : commands.entrySet()) {
                    if (commandNext.getKey().equalsIgnoreCase(command)) {
                        plugins.add(plugin.getName());
                    }
                    else {
                        for (final Map.Entry<String, Object> attributeNext : commandNext.getValue().entrySet()) {
                            if (attributeNext.getKey().equals("aliases")) {
                                final Object aliases = attributeNext.getValue();
                                if (aliases instanceof String) {
                                    if (!((String)aliases).equalsIgnoreCase(command)) {
                                        continue;
                                    }
                                    plugins.add(plugin.getName());
                                }
                                else {
                                    final List<String> array = (List<String>) aliases;
                                    for (final String str : array) {
                                        if (str.equalsIgnoreCase(command)) {
                                            plugins.add(plugin.getName());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return plugins;
    }
    
    public static boolean isIgnored(final Plugin plugin) {
        return isIgnored(plugin.getName());
    }
    
    public static boolean isIgnored(final String plugin) {
        return plugin.equalsIgnoreCase(Main.getInst().getName());
    }
    
    private static String load(final Plugin plugin) {
        return load(plugin.getName());
    }
    
    public static String load(final String name) {
        Plugin target = null;
        final File pluginDir = new File("plugins");
        if (!pluginDir.isDirectory()) {
            return "load.plugin-directory";
        }
        File pluginFile = new File(pluginDir, name + ".jar");
        if (!pluginFile.isFile()) {
            for (final File f : pluginDir.listFiles()) {
                if (f.getName().endsWith(".jar")) {
                    try {
                        final PluginDescriptionFile desc = Main.getInst().getPluginLoader().getPluginDescription(f);
                        if (desc.getName().equalsIgnoreCase(name)) {
                            pluginFile = f;
                            break;
                        }
                    }
                    catch (InvalidDescriptionException e3) {
                        return "load.cannot-find";
                    }
                }
            }
        }
        try {
            target = Bukkit.getPluginManager().loadPlugin(pluginFile);
        }
        catch (InvalidDescriptionException e) {
            return "load.invalid-description";
        }
        catch (InvalidPluginException e2) {
            return "load.invalid-plugin";
        }
        target.onLoad();
        Bukkit.getPluginManager().enablePlugin(target);
        return "load.loaded";
    }
    
    public static void reload(final Plugin plugin) {
        if (plugin != null) {
            unload(plugin);
            load(plugin);
        }
    }
    
    public static void reloadAll() {
        for (final Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (!isIgnored(plugin)) {
                reload(plugin);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
	public static String unload(final Plugin plugin) {
        final String name = plugin.getName();
        final PluginManager pluginManager = Bukkit.getPluginManager();
        SimpleCommandMap commandMap = null;
        List<Plugin> plugins = null;
        Map<String, Plugin> names = null;
        Map<String, Command> commands = null;
        Map<Event, SortedSet<RegisteredListener>> listeners = null;
        boolean reloadlisteners = true;
        if (pluginManager != null) {
            pluginManager.disablePlugin(plugin);
            try {
                final Field pluginsField = Bukkit.getPluginManager().getClass().getDeclaredField("plugins");
                pluginsField.setAccessible(true);
                plugins = (List<Plugin>)pluginsField.get(pluginManager);
                final Field lookupNamesField = Bukkit.getPluginManager().getClass().getDeclaredField("lookupNames");
                lookupNamesField.setAccessible(true);
                names = (Map<String, Plugin>)lookupNamesField.get(pluginManager);
                try {
                    final Field listenersField = Bukkit.getPluginManager().getClass().getDeclaredField("listeners");
                    listenersField.setAccessible(true);
                    listeners = (Map<Event, SortedSet<RegisteredListener>>)listenersField.get(pluginManager);
                }
                catch (Exception e3) {
                    reloadlisteners = false;
                }
                final Field commandMapField = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
                commandMapField.setAccessible(true);
                commandMap = (SimpleCommandMap)commandMapField.get(pluginManager);
                final Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
                knownCommandsField.setAccessible(true);
                commands = (Map<String, Command>)knownCommandsField.get(commandMap);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                return "failed";
            }
        }
        pluginManager.disablePlugin(plugin);
        if (plugins != null && plugins.contains(plugin)) {
            plugins.remove(plugin);
        }
        if (names != null && names.containsKey(name)) {
            names.remove(name);
        }
        if (listeners != null && reloadlisteners) {
            for (final SortedSet<RegisteredListener> set : listeners.values()) {
                set.removeIf(value -> value.getPlugin() == plugin);
            }
        }
        if (commandMap != null) {
            final Iterator<Map.Entry<String, Command>> it2 = commands.entrySet().iterator();
            while (it2.hasNext()) {
                final Map.Entry<String, Command> entry = it2.next();
                if (entry.getValue() instanceof PluginCommand) {
                    final PluginCommand c = (PluginCommand)entry.getValue();
                    if (c.getPlugin() != plugin) {
                        continue;
                    }
                    c.unregister(commandMap);
                    it2.remove();
                }
            }
        }
        final ClassLoader cl = plugin.getClass().getClassLoader();
        if (cl instanceof URLClassLoader) {
            try {
                ((URLClassLoader)cl).close();
            }
            catch (IOException ex) {
                Logger.getLogger(PluginUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.gc();
        return "unloaded";
    }
}
