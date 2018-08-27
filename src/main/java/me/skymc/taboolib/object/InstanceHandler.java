package me.skymc.taboolib.object;

import com.ilummc.tlib.util.Ref;
import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.fileutils.FileUtils;
import me.skymc.taboolib.listener.TListener;
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
public class InstanceHandler implements Listener {

    private static ConcurrentHashMap<String, Object> instance = new ConcurrentHashMap<>();

    public InstanceHandler() {
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
        if (!(plugin.equals(TabooLib.instance()) || TabooLib.isDependTabooLib(plugin))) {
            return;
        }
        for (Class pluginClass : FileUtils.getClasses(plugin)) {
            if (pluginClass.isAnnotationPresent(Instantiable.class)) {
                Instantiable instantiable = (Instantiable) pluginClass.getAnnotation(Instantiable.class);
                try {
                    instance.put(instantiable.value(), pluginClass.newInstance());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
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
