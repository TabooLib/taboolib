package io.izzel.taboolib.compat.kotlin;

import io.izzel.taboolib.TabooLibAPI;
import io.izzel.taboolib.util.Reflection;

/**
 * @Author sky
 * @Since 2019-09-19 14:27
 */
public class CompatKotlin {

    public static boolean isCompanion(Class clazz) {
        return clazz.getName().endsWith("$Companion");
    }

    public static Object getCompanion(Class clazz)  {
        try {
            return Reflection.getValue(null, TabooLibAPI.getPluginBridge().getClass(clazz.getName().substring(0, clazz.getName().indexOf("$Companion"))), true, "Companion");
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }
}
