package me.skymc.taboolib.common.loader;

import com.ilummc.tlib.logger.TLogger;
import com.ilummc.tlib.util.Ref;
import me.skymc.taboolib.TabooLibLoader;
import me.skymc.taboolib.listener.TListener;
import me.skymc.taboolib.methods.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author sky
 * @Since 2018-08-27 10:04
 */
@TListener
public class InstantiableLoader implements Listener {

    private static ConcurrentHashMap<String, Object> instance = new ConcurrentHashMap<>();

    public InstantiableLoader() {
        loadInstantiable();
    }

    @EventHandler
    public void onEnable(PluginEnableEvent e) {
        loadInstantiable(e.getPlugin());
    }

    @EventHandler
    public void onDisable(PluginDisableEvent e) {
        clear(e.getPlugin());
    }

    public static void clear(Plugin plugin) {
        instance.entrySet().stream().filter(entry -> Ref.getCallerPlugin(entry.getValue().getClass()).equals(plugin)).forEach(entry -> instance.remove(entry.getKey()));
    }

    public static void loadInstantiable() {
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            try {
                loadInstantiable(plugin);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void loadInstantiable(Plugin plugin) {
        TabooLibLoader.getPluginClasses(plugin).ifPresent(classes -> {
            for (Class pluginClass : classes) {
                if (pluginClass.isAnnotationPresent(Instantiable.class)) {
                    Instantiable instantiable = (Instantiable) pluginClass.getAnnotation(Instantiable.class);
                    try {
                        instance.put(instantiable.value(), ReflectionUtils.instantiateObject(pluginClass));
                    } catch (Exception e) {
                        TLogger.getGlobalLogger().warn("Instance Failed: " + pluginClass.getName());
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    // *********************************
    //
    //        Getter and Setter
    //
    // *********************************

    public static ConcurrentHashMap<String, Object> getInstance() {
        return instance;
    }

    public static Optional<Object> getInstance(String name) {
        return Optional.ofNullable(instance.get(name));
    }
}
