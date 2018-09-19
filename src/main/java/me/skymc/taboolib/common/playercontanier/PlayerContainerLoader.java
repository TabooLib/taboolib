package me.skymc.taboolib.common.playercontanier;

import com.ilummc.tlib.logger.TLogger;
import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.TabooLibLoader;
import me.skymc.taboolib.listener.TListener;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author sky
 * @Since 2018-09-14 23:45
 */
@TListener
public class PlayerContainerLoader implements Listener {

    private static Map<String, List<Container>> pluginContainer = new ConcurrentHashMap<>();

    PlayerContainerLoader() {
        load();
    }

    public static void load() {
        Arrays.stream(Bukkit.getPluginManager().getPlugins()).forEach(PlayerContainerLoader::load);
    }

    public static void load(Plugin plugin) {
        if (!(TabooLib.isTabooLib(plugin) || TabooLib.isDependTabooLib(plugin))) {
            return;
        }
        TabooLibLoader.getPluginClasses(plugin).ifPresent(classes -> {
            for (Class pluginClass : classes) {
                for (Field field : pluginClass.getDeclaredFields()) {
                    PlayerContainer annotation = field.getAnnotation(PlayerContainer.class);
                    if (annotation == null) {
                        continue;
                    }
                    field.setAccessible(true);
                    try {
                        pluginContainer.computeIfAbsent(plugin.getName(), name -> new ArrayList<>()).add(new Container(field.get(pluginClass), annotation.uniqueId()));
                    } catch (IllegalAccessException ignored) {
                    }
                }
            }
        });
    }

    public static void unload() {
        Arrays.stream(Bukkit.getPluginManager().getPlugins()).forEach(PlayerContainerLoader::unload);
    }

    public static void unload(Plugin plugin) {
        pluginContainer.remove(plugin.getName());
    }

    @EventHandler
    public void onEnable(PluginEnableEvent e) {
        load(e.getPlugin());
    }

    @EventHandler
    public void onDisable(PluginDisableEvent e) {
        unload(e.getPlugin());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        for (List<Container> containers : pluginContainer.values()) {
            for (Container container : containers) {
                if (container.getContainer() instanceof Map) {
                    ((Map) container.getContainer()).remove(container.isUniqueId() ? e.getPlayer().getUniqueId() : e.getPlayer().getName());
                } else if (container.getContainer() instanceof Collection) {
                    ((Collection) container.getContainer()).remove(container.isUniqueId() ? e.getPlayer().getUniqueId() : e.getPlayer().getName());
                } else {
                    TLogger.getGlobalLogger().error("Invalid Container: " + container.getContainer().getClass().getSimpleName());
                }
            }
        }
    }

}
