package io.izzel.taboolib.module.dependency;

import io.izzel.taboolib.TabooLibAPI;
import io.izzel.taboolib.common.plugin.InternalPlugin;
import io.izzel.taboolib.util.Ref;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * 依赖加载工具
 */
public class TDependencyLoader {

    private static Method ADD_URL_METHOD;

    static {
        try {
            ADD_URL_METHOD = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            ADD_URL_METHOD.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void addToPath(Plugin plugin, URL url) {
        try {
            ClassLoader loader = plugin instanceof InternalPlugin ? Bukkit.class.getClassLoader() : plugin.getClass().getClassLoader();
            if (TabooLibAPI.isForge()) {
                ADD_URL_METHOD.invoke(plugin.getClass().getClassLoader(), url);
            } else if ("LaunchClassLoader".equals(loader.getClass().getSimpleName())) {
                MethodHandle methodHandle = Ref.lookup().findVirtual(loader.getClass(), "addURL", MethodType.methodType(void.class, java.net.URL.class));
                methodHandle.invoke(loader, url);
            } else {
                Field ucpField = loader.getClass().getDeclaredField("ucp");
                long ucpOffset = Ref.getUnsafe().objectFieldOffset(ucpField);
                Object ucp = Ref.getUnsafe().getObject(loader, ucpOffset);
                MethodHandle methodHandle = Ref.lookup().findVirtual(ucp.getClass(), "addURL", MethodType.methodType(void.class, java.net.URL.class));
                methodHandle.invoke(ucp, url);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static synchronized void addToPath(Plugin plugin, File file) {
        try {
            addToPath(plugin, file.toURI().toURL());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
