package io.izzel.taboolib.module.dependency;

import io.izzel.taboolib.common.plugin.InternalPlugin;
import io.izzel.taboolib.util.Ref;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class TDependencyLoader {

    public static synchronized void addToPath(Plugin plugin, URL url) {
        try {
            ClassLoader loader = plugin instanceof InternalPlugin ? Bukkit.class.getClassLoader() : plugin.getClass().getClassLoader();
            if ("LaunchClassLoader".equals(loader.getClass().getSimpleName())) {
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
