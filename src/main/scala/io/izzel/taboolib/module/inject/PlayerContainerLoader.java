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
 * @author sky
 * @since 2018-09-14 23:45
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
        for (Map.Entry<String, List<Container>> containers : containersMap.entrySet()) {
            for (Container container : containers.getValue()) {
                if (container.isInstanceOf(Map.class)) {
                    container.<Map<?, ?>>cast().remove(container.namespace(e.getPlayer()));
                } else if (container.isInstanceOf(Collection.class)) {
                    container.<Collection<?>>cast().remove(container.namespace(e.getPlayer()));
                } else if (container.isInstanceOf(Releasable.class)) {
                    container.<Releasable>cast().release(e.getPlayer(), container.namespace(e.getPlayer()).toString());
                } else {
                    TLogger.getGlobalLogger().error("Unsupported container: " + container);
                }
            }
        }
    }
}
