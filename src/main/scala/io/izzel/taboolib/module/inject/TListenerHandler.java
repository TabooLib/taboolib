package io.izzel.taboolib.module.inject;

import io.izzel.taboolib.TabooLibLoader;
import io.izzel.taboolib.Version;
import io.izzel.taboolib.cronus.util.StringExpression;
import io.izzel.taboolib.util.Coerce;
import io.izzel.taboolib.util.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author sky
 * @since 2018-08-22 13:48
 */
public class TListenerHandler implements TabooLibLoader.Loader {

    private static final Map<String, List<Listener>> listeners = new HashMap<>();
    private static final Map<String, Map<Class<?>, Listener>> listenerInstances = new HashMap<>();

    @Override
    public void postLoad(Plugin plugin, Class<?> pluginClass) {
        if (Listener.class.isAssignableFrom(pluginClass) && pluginClass.isAnnotationPresent(TListener.class)) {
            TListener annotation = pluginClass.getAnnotation(TListener.class);
            if (new StringExpression(annotation.version()).isSelect(Version.getCurrentVersionInt())) {
                if (annotation.depend().length != 0) {
                    for (String depend : annotation.depend()) {
                        if (depend.length() != 0 && Bukkit.getPluginManager().getPlugin(depend) == null) {
                            return;
                        }
                    }
                }
                Listener listener = (Listener) TInjectHelper.getInstance(pluginClass, plugin).get(0);
                listeners.computeIfAbsent(plugin.getName(), name -> new ArrayList<>()).add(listener);
            }
        }
    }

    @Override
    public void activeLoad(Plugin plugin, Class<?> pluginClass) {
        if (Listener.class.isAssignableFrom(pluginClass) && pluginClass.isAnnotationPresent(TListener.class)) {
            TListener annotation = pluginClass.getAnnotation(TListener.class);
            if (new StringExpression(annotation.version()).isSelect(Version.getCurrentVersionInt())) {
                if (annotation.depend().length != 0) {
                    for (String depend : annotation.depend()) {
                        if (depend.length() != 0 && Bukkit.getPluginManager().getPlugin(depend) == null) {
                            return;
                        }
                    }
                }
                try {
                    Listener listener = (Listener) TInjectHelper.getInstance(pluginClass, plugin).get(0);
                    if (annotation.condition().length() != 0) {
                        Object invokeMethod = Reflection.invokeMethod(listener, pluginClass, annotation.condition());
                        if (!Coerce.toBoolean(invokeMethod)) {
                            return;
                        }
                    }
                    if (annotation.register().length() != 0) {
                        Reflection.invokeMethod(listener, pluginClass, annotation.register());
                    }
                    Bukkit.getPluginManager().registerEvents(listener, plugin);
                    listenerInstances.computeIfAbsent(plugin.getName(), name -> new HashMap<>()).put(pluginClass, listener);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void unload(Plugin plugin, Class<?> pluginClass) {
        if (Listener.class.isAssignableFrom(pluginClass) && pluginClass.isAnnotationPresent(TListener.class)) {
            Map<Class<?>, Listener> classes = listenerInstances.get(plugin.getName());
            if (classes != null && classes.containsKey(pluginClass)) {
                Listener listener = classes.get(pluginClass);
                TListener annotation = pluginClass.getAnnotation(TListener.class);
                if (annotation.register().length() != 0) {
                    try {
                        Reflection.invokeMethod(listener, pluginClass, annotation.register());
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
                HandlerList.unregisterAll(listener);
            }
        }
    }

    public static List<Listener> getInstance(Plugin plugin, Class<?> pluginClass) {
        List<Listener> list = TListenerHandler.listeners.get(plugin.getName());
        return list == null ? Collections.emptyList() : list.stream().filter(listener -> pluginClass.equals(listener.getClass())).collect(Collectors.toList());
    }

    public static Map<String, List<Listener>> getListeners() {
        return listeners;
    }

    public static Map<String, Map<Class<?>, Listener>> getListenerInstance() {
        return listenerInstances;
    }
}
