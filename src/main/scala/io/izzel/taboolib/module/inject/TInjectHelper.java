package io.izzel.taboolib.module.inject;

import com.google.common.collect.Lists;
import io.izzel.taboolib.PluginLoader;
import io.izzel.taboolib.compat.kotlin.CompatKotlin;
import io.izzel.taboolib.module.locale.logger.TLogger;
import io.izzel.taboolib.util.Reflection;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

/**
 * @author sky
 * @since 2019-08-17 23:22
 */
public class TInjectHelper {

    enum State {

        PRE, POST, ACTIVE, CANCEL
    }

    public static String fromState(TInject inject, State state) {
        switch (state) {
            case PRE:
                return inject.load();
            case POST:
                return inject.init();
            case ACTIVE:
                return inject.active();
            default:
                return inject.cancel();
        }
    }

    public static List<Object> getInstance(Class<?> pluginClass, Plugin plugin) {
        List<Object> instance = Lists.newArrayList();
        if (pluginClass.isInstance(PluginLoader.get(plugin))) {
            instance.add(PluginLoader.get(plugin));
        }
        if (CompatKotlin.getInstance(pluginClass) != null) {
            instance.add(CompatKotlin.getInstance(pluginClass));
        }
        if (CompatKotlin.isCompanion(pluginClass)) {
            instance.add(CompatKotlin.getCompanion(pluginClass, plugin));
        }
        TInjectCreator.getInstanceMap().entrySet()
                .stream()
                .filter(e -> e.getKey().getType().equals(pluginClass))
                .forEach(e -> instance.add(e.getValue().getInstance()));
        instance.addAll(TListenerLoader.getInstance(plugin, pluginClass));
        if (instance.isEmpty()) {
            try {
                instance.add(Reflection.instantiateObject(pluginClass));
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    public static List<Object> getInstance(Field field, Class<?> pluginClass, Plugin plugin) {
        List<Object> instance = Lists.newArrayList();
        if (Modifier.isStatic(field.getModifiers())) {
            instance.add(null);
        } else {
            if (pluginClass.isInstance(PluginLoader.get(plugin))) {
                instance.add(PluginLoader.get(plugin));
            }
            TInjectCreator.getInstanceMap().entrySet()
                    .stream()
                    .filter(e -> e.getKey().getType().equals(pluginClass))
                    .forEach(e -> instance.add(e.getValue().getInstance()));
            instance.addAll(TListenerLoader.getInstance(plugin, pluginClass));
        }
        if (instance.isEmpty()) {
            TLogger.getGlobalLogger().warn("No instance of " + field.getName() + " (" + pluginClass.getSimpleName() + ")");
        }
        return instance;
    }

    public static List<Object> getInstance(Method method, Class<?> pluginClass, Plugin plugin) {
        List<Object> instance = Lists.newArrayList();
        if (Modifier.isStatic(method.getModifiers())) {
            instance.add(null);
        } else {
            if (pluginClass.isInstance(PluginLoader.get(plugin))) {
                instance.add(PluginLoader.get(plugin));
            }
            if (CompatKotlin.getInstance(pluginClass) != null) {
                instance.add(CompatKotlin.getInstance(pluginClass));
            }
            if (CompatKotlin.isCompanion(pluginClass)) {
                instance.add(CompatKotlin.getCompanion(pluginClass, plugin));
            }
            TInjectCreator.getInstanceMap().entrySet()
                    .stream()
                    .filter(e -> e.getKey().getType().equals(pluginClass))
                    .forEach(e -> instance.add(e.getValue().getInstance()));
            instance.addAll(TListenerLoader.getInstance(plugin, pluginClass));
        }
        if (instance.isEmpty()) {
            TLogger.getGlobalLogger().warn("No instance of " + method.getName() + " (" + pluginClass.getSimpleName() + ")");
        }
        return instance;
    }
}
