package io.izzel.taboolib.module.inject;

import io.izzel.taboolib.TabooLibLoader;
import io.izzel.taboolib.module.locale.logger.TLogger;
import io.izzel.taboolib.util.Ref;
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

    private static final Map<String, List<Container>> containersMap = new ConcurrentHashMap<>();

    @Override
    public void postLoad(Plugin plugin, Class<?> pluginClass) {
        for (Field field : pluginClass.getDeclaredFields()) {
            PlayerContainer annotation = field.getAnnotation(PlayerContainer.class);
            if (annotation == null) {
                continue;
            }
            field.setAccessible(true);
            for (Object instance : TInjectHelper.getInstance(field, pluginClass, plugin)) {
                try {
                    containersMap.computeIfAbsent(plugin.getName(), name -> new ArrayList<>()).add(new Container(Ref.getField(instance, field), annotation.uniqueId()));
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }

    @Override
    public void unload(Plugin plugin, Class<?> pluginClass) {
        containersMap.remove(plugin.getName());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        for (List<Container> containers : containersMap.values()) {
            for (Container container : containers) {
                if (container.getContainer() instanceof Map) {
                    container.<Map<?, ?>>as().remove(container.isUniqueId() ? e.getPlayer().getUniqueId() : e.getPlayer().getName());
                } else if (container.getContainer() instanceof Collection) {
                    container.<Collection<?>>as().remove(container.isUniqueId() ? e.getPlayer().getUniqueId() : e.getPlayer().getName());
                } else if (container.getContainer() instanceof Releasable) {
                    container.<Releasable>as().release(e.getPlayer(), container.isUniqueId() ? e.getPlayer().getUniqueId().toString() : e.getPlayer().getName());
                } else {
                    TLogger.getGlobalLogger().error("Unsupported container type: " + container.getContainer().getClass().getSimpleName());
                }
            }
        }
    }
}
