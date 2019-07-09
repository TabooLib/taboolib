package io.izzel.taboolib.common.plugin.bridge;

import io.izzel.taboolib.util.Reflection;
import org.bukkit.Bukkit;

import java.lang.reflect.Method;

/**
 * @Author 坏黑
 * @Since 2019-07-09 17:43
 */
public class BridgeLoader extends ClassLoader {

    private static Method findClass;
    private static ClassLoader pluginClassLoader;

    public static BridgeLoader getInstance() {
        return new BridgeLoader();
    }

    private BridgeLoader() {
        super(BridgeLoader.class.getClassLoader());
        try {
            findClass = Reflection.getMethod(ClassLoader.class, "findClass", String.class);
            pluginClassLoader = Bukkit.getPluginManager().getPlugins()[0].getClass().getClassLoader();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            Object o = findClass.invoke(pluginClassLoader, name);
            if (o != null) {
                return (Class<?>) o;
            }
        } catch (Throwable ignored) {
        }
        return super.findClass(name);
    }

    public static Class<?> createNewClass(String name, byte[] arr) {
        return getInstance().defineClass(name, arr, 0, arr.length, BridgeLoader.class.getProtectionDomain());
    }
}
