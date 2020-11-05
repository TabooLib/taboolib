package io.izzel.taboolib.module.inject;

import com.google.common.collect.Lists;
import io.izzel.taboolib.PluginLoader;
import io.izzel.taboolib.compat.kotlin.CompatKotlin;
import io.izzel.taboolib.module.locale.logger.TLogger;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

/**
 * @Author sky
 * @Since 2019-08-17 23:22
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

    @NotNull
    public static List<Object> getInstance(Field field, Class<?> pluginClass, Plugin plugin) {
        List<Object> instance = Lists.newArrayList();
        if (Modifier.isStatic(field.getModifiers())) {
            instance.add(null);
        } else if (!Modifier.isStatic(field.getModifiers())) {
            instance.addAll(getInstance(pluginClass, plugin, false));
        }
        if (instance.isEmpty()) {
            TLogger.getGlobalLogger().error(field.getName() + " is not a static field. (" + pluginClass.getName() + ")");
        }
        return instance;
    }

    public static List<Object> getInstance(Method method, Class<?> pluginClass, Plugin plugin) {
        List<Object> instance = Lists.newArrayList();
        if (Modifier.isStatic(method.getModifiers())) {
            instance.add(null);
        } else if (!Modifier.isStatic(method.getModifiers())) {
            instance.addAll(getInstance(pluginClass, plugin, false));
        }
        if (instance.isEmpty()) {
            TLogger.getGlobalLogger().error(method.getName() + " is not a static method. (" + pluginClass.getName() + ")");
        }
        return instance;
    }

    @NotNull
    public static List<Object> getInstance(Class<?> pluginClass, Plugin plugin, boolean compat) {
        List<Object> instance = Lists.newArrayList();
        // Main
        if (pluginClass.equals(PluginLoader.get(plugin).getClass())) {
            instance.add(PluginLoader.get(plugin));
        }
        // Instanced by TInject
        if (TInjectCreator.getInstanceMap().entrySet().stream().anyMatch(e -> e.getKey().getType().equals(pluginClass))) {
            TInjectCreator.getInstanceMap().entrySet().stream().filter(e -> e.getKey().getType().equals(pluginClass)).forEach(i -> instance.add(i.getValue().getInstance()));
        }
        // Instanced by TListener
        if (plugin != null) {
            instance.addAll(TListenerHandler.getInstance(plugin, pluginClass));
        }
        if (compat) {
            // Object
            if (CompatKotlin.getInstance(pluginClass) != null) {
                instance.add(CompatKotlin.getInstance(pluginClass));
            }
            // Companion Object
            else if (CompatKotlin.isCompanion(pluginClass)) {
                instance.add(CompatKotlin.getCompanion(pluginClass));
            }
        }
        return instance;
    }
}
