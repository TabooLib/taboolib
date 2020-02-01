package io.izzel.taboolib.module.dependency;

import io.izzel.taboolib.common.plugin.InternalPlugin;
import io.izzel.taboolib.util.Ref;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
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
            Field ucpField = loader.getClass().getDeclaredField("ucp");
            long ucpOffset = Ref.UNSAFE.objectFieldOffset(ucpField);
            Object ucp = Ref.UNSAFE.getObject(loader, ucpOffset);
            Method addURL = ucp.getClass().getMethod("addURL", URL.class);
            addURL.invoke(ucp, url);
        } catch (Exception e) {
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
