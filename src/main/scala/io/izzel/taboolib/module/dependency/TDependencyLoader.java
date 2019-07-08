package io.izzel.taboolib.module.dependency;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class TDependencyLoader {

    public static synchronized void addToPath(Plugin plugin, URL url) {
        try {
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(Bukkit.class.getClassLoader(), url);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
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
