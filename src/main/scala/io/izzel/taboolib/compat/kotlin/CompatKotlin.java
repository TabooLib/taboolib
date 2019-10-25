package io.izzel.taboolib.compat.kotlin;

import io.izzel.taboolib.TabooLibAPI;
import io.izzel.taboolib.util.Reflection;

/**
 * @Author sky
 * @Since 2019-09-19 14:27
 */
public class CompatKotlin {

    public static boolean isCompanion(Class<?> pluginClass) {
        return pluginClass.getName().endsWith("$Companion");
    }

    public static Object getCompanion(Class<?> pluginClass)  {
        try {
            return Reflection.getValue(null, TabooLibAPI.getPluginBridge().getClass(pluginClass.getName().substring(0, pluginClass.getName().indexOf("$Companion"))), true, "Companion");
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    public static Object getInstance(Class<?> pluginClass)  {
        try {
            return Reflection.getValue(null, pluginClass, true, "INSTANCE");
        } catch (Throwable ignored) {
        }
        return null;
    }
}
