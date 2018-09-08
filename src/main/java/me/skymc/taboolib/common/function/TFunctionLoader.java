package me.skymc.taboolib.common.function;

import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.TabooLibLoader;
import me.skymc.taboolib.listener.TListener;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @Author sky
 * @Since 2018-09-08 14:00
 */
@TListener
public class TFunctionLoader implements Listener {

    private static HashMap<String, List<Class>> pluginFunction = new HashMap<>();

    TFunctionLoader() {
        loadFunction();
    }

    public static void loadFunction() {
        Arrays.stream(Bukkit.getPluginManager().getPlugins()).forEach(TFunctionLoader::loadFunction);
    }

    public static void loadFunction(Plugin plugin) {
        if (!(TabooLib.isTabooLib(plugin) || TabooLib.isDependTabooLib(plugin))) {
            return;
        }
        TabooLibLoader.getPluginClasses(plugin).ifPresent(classes -> {
            for (Class pluginClass : classes) {
                if (pluginClass.isAnnotationPresent(TFunction.class)) {
                    TFunction function = (TFunction) pluginClass.getAnnotation(TFunction.class);
                    try {
                        Method method = pluginClass.getDeclaredMethod(function.enable());
                        if (method == null) {
                            continue;
                        }
                        method.setAccessible(true);
                        method.invoke(pluginClass.newInstance());
                        pluginFunction.computeIfAbsent(plugin.getName(), name -> new ArrayList<>()).add(pluginClass);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public static void unloadFunction() {
        Arrays.stream(Bukkit.getPluginManager().getPlugins()).forEach(TFunctionLoader::unloadFunction);
    }

    public static void unloadFunction(Plugin plugin) {
        Optional.ofNullable(pluginFunction.remove(plugin.getName())).ifPresent(classes -> {
            for (Class pluginClass : classes) {
                if (pluginClass.isAnnotationPresent(TFunction.class)) {
                    TFunction function = (TFunction) pluginClass.getAnnotation(TFunction.class);
                    try {
                        Method method = pluginClass.getDeclaredMethod(function.disable());
                        if (method == null) {
                            continue;
                        }
                        method.setAccessible(true);
                        method.invoke(pluginClass.newInstance());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @EventHandler
    public void onEnable(PluginEnableEvent e) {
        loadFunction(e.getPlugin());
    }

    @EventHandler
    public void onDisable(PluginDisableEvent e) {
        unloadFunction(e.getPlugin());
    }
}
