package io.izzel.taboolib.module.inject;

import com.google.common.collect.Lists;
import io.izzel.taboolib.PluginLoader;
import io.izzel.taboolib.compat.kotlin.CompatKotlin;
import io.izzel.taboolib.module.locale.logger.TLogger;
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

    public static List<Object> getInstance(Field field, Class<?> pluginClass, Plugin plugin) {
        List<Object> instance = Lists.newArrayList();
        // Static
        if (Modifier.isStatic(field.getModifiers())) {
            instance.add(null);
        }
        // No Static
        else if (!Modifier.isStatic(field.getModifiers())) {
            // Main
            if (pluginClass.equals(PluginLoader.get(plugin).getClass())) {
                instance.add(PluginLoader.get(plugin));
            }
            // TInject
            if (TInjectCreator.getInstanceMap().entrySet().stream().anyMatch(e -> e.getKey().getType().equals(pluginClass))) {
                TInjectCreator.getInstanceMap().entrySet().stream().filter(e -> e.getKey().getType().equals(pluginClass)).forEach(i -> instance.add(i.getValue().getInstance()));
            }
            // TListener
            instance.addAll(TListenerHandler.getInstance(plugin, pluginClass));
        }
        // Nothing
        if (instance.isEmpty()) {
            TLogger.getGlobalLogger().error(field.getName() + " is not a static field. (" + pluginClass.getName() + ")");
        }
        return instance;
    }

    public static List<Object> getInstance(Method method, Class<?> pluginClass, Plugin plugin) {
        List<Object> instance = Lists.newArrayList();
        // Static
        if (Modifier.isStatic(method.getModifiers())) {
            instance.add(null);
        }
        // No Static
        else if (!Modifier.isStatic(method.getModifiers())) {
            // Object
            if (CompatKotlin.getInstance(pluginClass) != null) {
                instance.add(CompatKotlin.getInstance(pluginClass));
            }
            // Companion Object
            else if (CompatKotlin.isCompanion(pluginClass)) {
                instance.add(CompatKotlin.getCompanion(pluginClass));
            }
            // TInject
            if (TInjectCreator.getInstanceMap().entrySet().stream().anyMatch(e -> e.getKey().getType().equals(pluginClass))) {
                TInjectCreator.getInstanceMap().entrySet().stream().filter(e -> e.getKey().getType().equals(pluginClass)).forEach(i -> instance.add(i.getValue().getInstance()));
            }
            // TListener
            if (plugin != null) {
                instance.addAll(TListenerHandler.getInstance(plugin, pluginClass));
            }
            // Main
            Object redefine = PluginLoader.get(plugin);
            if (redefine != null && redefine.getClass().equals(pluginClass)) {
                instance.add(PluginLoader.get(plugin));
            }
        }
        // Nothing
        if (instance.isEmpty()) {
            TLogger.getGlobalLogger().error(method.getName() + " is not a static method. (" + pluginClass.getName() + ")");
        }
        return instance;
    }
}
