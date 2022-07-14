package io.izzel.taboolib.compat.kotlin;

import io.izzel.taboolib.TabooLibLoader;
import io.izzel.taboolib.util.Reflection;
import org.bukkit.plugin.Plugin;

/**
 * @author sky
 * @since 2019-09-19 14:27
 */
public class CompatKotlin {

    public static boolean isCompanion(Class<?> pluginClass) {
        return pluginClass.getName().endsWith("$Companion");
    }

    public static Object getCompanion(Class<?> pluginClass, Plugin plugin) {
        try {
            String name = pluginClass.getName().substring(0, pluginClass.getName().indexOf("$Companion"));
            for (Class<?> clazz : TabooLibLoader.getPluginClassSafely(plugin)) {
                if (clazz.getName().equals(name)) {
                    return Reflection.getValue(null, clazz, true, "Companion");
                }
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    public static Object getInstance(Class<?> pluginClass) {
        try {
            return Reflection.getValue(null, pluginClass, true, "INSTANCE");
        } catch (Throwable ignored) {
        }
        return null;
    }
}
