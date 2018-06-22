package me.skymc.taboolib.javashell.utils;

import me.skymc.taboolib.message.MsgUtils;
import org.bukkit.Bukkit;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarUtils {

    public static boolean extractFromJar(final String fileName, final String dest) throws IOException {
        if (getRunningJar() == null) {
            return false;
        }
        final File file = new File(dest);
        if (file.isDirectory()) {
            file.mkdir();
            return false;
        }
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }

        final JarFile jar = getRunningJar();
        final Enumeration<JarEntry> e = jar.entries();
        while (e.hasMoreElements()) {
            final JarEntry je = e.nextElement();
            if (!je.getName().contains(fileName)) {
                continue;
            }
            final InputStream in = new BufferedInputStream(jar.getInputStream(je));
            final OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
            copyInputStream(in, out);
            jar.close();
            return true;
        }
        jar.close();
        return false;
    }

    private static void copyInputStream(final InputStream in, final OutputStream out) throws IOException {
        try {
            final byte[] buff = new byte[4096];
            int n;
            while ((n = in.read(buff)) > 0) {
                out.write(buff, 0, n);
            }
        } finally {
            out.flush();
            out.close();
            in.close();
        }
    }

    @Deprecated
    public static URL getJarUrl(final File file) throws IOException {
        return new URL("jar:" + file.toURI().toURL().toExternalForm() + "!/");
    }

    public static JarFile getRunningJar() throws IOException {
        if (!RUNNING_FROM_JAR) {
            // null if not running from jar
            return null;
        }
        String path = new File(JarUtils.class.getProtectionDomain().getCodeSource().getLocation().getPath())
                .getAbsolutePath();
        path = URLDecoder.decode(path, "UTF-8");
        return new JarFile(path);
    }

    private static boolean RUNNING_FROM_JAR = false;

    static {
        final URL resource = JarUtils.class.getClassLoader().getResource("plugin.yml");
        if (resource != null) {
            RUNNING_FROM_JAR = true;
        }
    }

    @Deprecated
    public static void addClassPath(final URL url) {
        final URLClassLoader sysloader = (URLClassLoader) Bukkit.class.getClassLoader();
        final Class<URLClassLoader> sysclass = URLClassLoader.class;
        try {
            final Method method = sysclass.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(sysloader, url);
        } catch (Throwable t) {
            MsgUtils.warn("无法添加添加 &4" + url + "&c 到运行库");
            MsgUtils.warn(t.getMessage());
        }
    }

}