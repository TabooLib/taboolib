//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PluginUtils {

    private PluginUtils() {
    }

    public static void enable(Plugin plugin) {
        if (plugin != null && !plugin.isEnabled()) {
            Bukkit.getPluginManager().enablePlugin(plugin);
        }

    }

    public static void disable(Plugin plugin) {
        if (plugin != null && plugin.isEnabled()) {
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }

    public static void enableAll() {
        Arrays.stream(Bukkit.getPluginManager().getPlugins()).filter(plugin -> isIgnored(plugin)).forEach(PluginUtils::enable);
    }

    public static void disableAll() {
        Arrays.stream(Bukkit.getPluginManager().getPlugins()).filter(plugin -> isIgnored(plugin)).forEach(PluginUtils::disable);
    }

    public static String getFormattedName(Plugin plugin) {
        return getFormattedName(plugin, false);
    }

    public static String getFormattedName(Plugin plugin, boolean includeVersions) {
        ChatColor color = plugin.isEnabled() ? ChatColor.GREEN : ChatColor.RED;
        String pluginName = color + plugin.getName();
        if (includeVersions) {
            pluginName = pluginName + " (" + plugin.getDescription().getVersion() + ")";
        }
        return pluginName;
    }

    public static Plugin getPluginByName(String[] args, int start) {
        return getPluginByName(consolidateStrings(args, start));
    }

    public static Plugin getPluginByName(String name) {
        return Arrays.stream(Bukkit.getPluginManager().getPlugins(), 0, Bukkit.getPluginManager().getPlugins().length).filter(plugin -> name.equalsIgnoreCase(plugin.getName())).findFirst().orElse(null);
    }

    public static List<String> getPluginNames(boolean fullName) {
        List<String> plugins;
        plugins = Arrays.stream(Bukkit.getPluginManager().getPlugins()).map(plugin -> fullName ? plugin.getDescription().getFullName() : plugin.getName()).collect(Collectors.toList());
        return plugins;
    }

    public static String getPluginVersion(String name) {
        Plugin plugin = getPluginByName(name);
        return plugin != null && plugin.getDescription() != null ? plugin.getDescription().getVersion() : null;
    }

    public static String getUsages(Plugin plugin) {
        List<String> parsedCommands = new ArrayList();
        Map commands = plugin.getDescription().getCommands();
        if (commands != null) {
            for (Object o : commands.entrySet()) {
                Entry thisEntry = (Entry) o;
                if (thisEntry != null) {
                    parsedCommands.add((String) thisEntry.getKey());
                }
            }
        }
        return parsedCommands.isEmpty() ? "No commands registered." : Joiner.on(", ").join(parsedCommands);
    }

    public static List<String> findByCommand(String command) {
        List<String> plugins = new ArrayList();

        label60:
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            Map<String, Map<String, Object>> commands = plugin.getDescription().getCommands();
            if (commands != null) {
                Iterator commandIterator = commands.entrySet().iterator();

                while (true) {
                    label55:
                    while (true) {
                        if (!commandIterator.hasNext()) {
                            continue label60;
                        }

                        Entry<String, Map<String, Object>> commandNext = (Entry) commandIterator.next();
                        if (commandNext.getKey().equalsIgnoreCase(command)) {
                            plugins.add(plugin.getName());
                        } else {
                            Iterator attributeIterator = ((Map) commandNext.getValue()).entrySet().iterator();

                            while (true) {
                                while (true) {
                                    Entry attributeNext;
                                    if (!attributeIterator.hasNext()) {
                                        continue label55;
                                    }

                                    attributeNext = (Entry) attributeIterator.next();
                                    while (!"aliases".equals(attributeNext.getKey())) {
                                        if (!attributeIterator.hasNext()) {
                                            continue label55;
                                        }

                                        attributeNext = (Entry) attributeIterator.next();
                                    }

                                    Object aliases = attributeNext.getValue();
                                    if (aliases instanceof String) {
                                        if (((String) aliases).equalsIgnoreCase(command)) {
                                            plugins.add(plugin.getName());
                                        }
                                    } else {
                                        ((List<String>) aliases).stream().filter(str -> str.equalsIgnoreCase(command)).map(str -> plugin.getName()).forEach(plugins::add);
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

    public static boolean isIgnored(Plugin plugin) {
        return plugin.equals(Main.getInst());
    }

    private static String load(Plugin plugin) {
        return load(plugin.getName());
    }

    /**
     * 返回内容：
     *
     * plugin-directory —— 插件目录不存在
     * cannot-find —— 插件不存在
     * invalid-description —— 无效的描述
     * invalid-plugin —— 无效的插件
     * loaded —— 载入成功
     */
    public static String load(String name) {
        Plugin target;
        File pluginDir = new File("plugins");
        if (!pluginDir.isDirectory()) {
            return "plugin-directory";
        } else {
            File pluginFile = new File(pluginDir, name + ".jar");
            if (!pluginFile.isFile()) {
                for (File f : Objects.requireNonNull(pluginDir.listFiles())) {
                    if (f.getName().endsWith(".jar")) {
                        try {
                            PluginDescriptionFile desc = Main.getInst().getPluginLoader().getPluginDescription(f);
                            if (desc.getName().equalsIgnoreCase(name)) {
                                pluginFile = f;
                                break;
                            }
                        } catch (InvalidDescriptionException var11) {
                            return "cannot-find";
                        }
                    }
                }
            }

            try {
                target = Bukkit.getPluginManager().loadPlugin(pluginFile);
            } catch (InvalidDescriptionException var9) {
                return "invalid-description";
            } catch (InvalidPluginException var10) {
                return "invalid-plugin";
            }

            target.onLoad();
            Bukkit.getPluginManager().enablePlugin(target);
            return "loaded";
        }
    }

    public static void reload(Plugin plugin) {
        if (plugin != null) {
            unload(plugin);
            load(plugin);
        }

    }

    public static void reloadAll() {
        Arrays.stream(Bukkit.getPluginManager().getPlugins(), 0, Bukkit.getPluginManager().getPlugins().length).filter(PluginUtils::isIgnored).forEach(PluginUtils::reload);
    }

    /**
     * 返回内容：
     *
     * failed —— 卸载失败
     * unloaded —— 卸载成功
     */
    public static String unload(Plugin plugin) {
        String name = plugin.getName();
        PluginManager pluginManager = Bukkit.getPluginManager();
        SimpleCommandMap commandMap = null;
        List<Plugin> plugins = null;
        Map<String, Plugin> names = null;
        Map<String, Command> commands = null;
        Map<Event, SortedSet<RegisteredListener>> listeners = null;
        if (pluginManager != null) {
            pluginManager.disablePlugin(plugin);

            try {
                Field pluginsField = Bukkit.getPluginManager().getClass().getDeclaredField("plugins");
                pluginsField.setAccessible(true);
                plugins = (List) pluginsField.get(pluginManager);
                Field lookupNamesField = Bukkit.getPluginManager().getClass().getDeclaredField("lookupNames");
                lookupNamesField.setAccessible(true);
                names = (Map) lookupNamesField.get(pluginManager);

                Field commandMapField;
                try {
                    commandMapField = Bukkit.getPluginManager().getClass().getDeclaredField("listeners");
                    commandMapField.setAccessible(true);
                    listeners = (Map) commandMapField.get(pluginManager);
                } catch (Exception ignored) {
                }

                commandMapField = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
                commandMapField.setAccessible(true);
                commandMap = (SimpleCommandMap) commandMapField.get(pluginManager);
                Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
                knownCommandsField.setAccessible(true);
                commands = (Map) knownCommandsField.get(commandMap);
            } catch (NoSuchFieldException | IllegalAccessException var15) {
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

        Iterator it;
        if (listeners != null) {
            it = listeners.values().iterator();

            while (it.hasNext()) {
                SortedSet<RegisteredListener> set = (SortedSet) it.next();

                while (it.hasNext()) {
                    RegisteredListener value = (RegisteredListener) it.next();
                    if (value.getPlugin() == plugin) {
                        it.remove();
                    }
                }
            }
        }

        if (commandMap != null) {
            it = commands.entrySet().iterator();

            while (it.hasNext()) {
                Entry<String, Command> entry = (Entry) it.next();
                if (entry.getValue() instanceof PluginCommand) {
                    PluginCommand c = (PluginCommand) entry.getValue();
                    if (c.getPlugin() == plugin) {
                        c.unregister(commandMap);
                        it.remove();
                    }
                }
            }
        }

        ClassLoader cl = plugin.getClass().getClassLoader();
        if (cl instanceof URLClassLoader) {
            try {
                ((URLClassLoader) cl).close();
            } catch (IOException var13) {
                Logger.getLogger(PluginUtils.class.getName()).log(Level.SEVERE, null, var13);
            }
        }
        return "unloaded";
    }

    private static String consolidateStrings(String[] args, int start) {
        String ret = "";
        if (args.length > start + 1) {
            ret = IntStream.range(start + 1, args.length).mapToObj(i -> " " + args[i]).collect(Collectors.joining("", args[start], ""));
        }
        return ret;
    }
}
