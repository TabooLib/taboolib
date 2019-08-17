package io.izzel.taboolib.module.inject;

import io.izzel.taboolib.TabooLibLoader;
import io.izzel.taboolib.module.locale.logger.TLogger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author sky
 * @Since 2018-09-14 23:45
 */
@TListener
public class PlayerContainerLoader implements Listener, TabooLibLoader.Loader {

    static Map<String, List<Container>> pluginContainer = new ConcurrentHashMap<>();

    @Override
    public void postLoad(Plugin plugin, Class<?> pluginClass) {
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

    @Override
    public void unload(Plugin plugin, Class<?> pluginClass) {
        pluginContainer.remove(plugin.getName());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        pluginContainer.values().stream().flatMap(Collection::stream).forEach(container -> {
            if (container.getContainer() instanceof Map) {
                ((Map) container.getContainer()).remove(container.isUniqueId() ? e.getPlayer().getUniqueId() : e.getPlayer().getName());
            } else if (container.getContainer() instanceof Collection) {
                ((Collection) container.getContainer()).remove(container.isUniqueId() ? e.getPlayer().getUniqueId() : e.getPlayer().getName());
            } else {
                TLogger.getGlobalLogger().error("Invalid Container: " + container.getContainer().getClass().getSimpleName());
            }
        });
    }
}
