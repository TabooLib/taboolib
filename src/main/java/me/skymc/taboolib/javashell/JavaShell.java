package me.skymc.taboolib.javashell;

import com.ilummc.tlib.dependency.TDependencyLoader;
import me.skymc.taboolib.Main;
import me.skymc.taboolib.message.MsgUtils;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

@Deprecated
public class JavaShell {

    private static String paths = "";
    private static File javaShellFolder;
    private static File scriptFolder;
    private static File cacheFolder;
    private static File libFolder;
    private static HashMap<String, Class<?>> shells = new HashMap<>();

    public static String getPaths() {
        return paths;
    }

    public static void setPaths(String paths) {
        JavaShell.paths = paths;
    }

    public static File getJavaShellFolder() {
        return javaShellFolder;
    }

    public static void setJavaShellFolder(File javaShellFolder) {
        JavaShell.javaShellFolder = javaShellFolder;
    }

    public static File getScriptFolder() {
        return scriptFolder;
    }

    public static void setScriptFolder(File scriptFolder) {
        JavaShell.scriptFolder = scriptFolder;
    }

    public static File getCacheFolder() {
        return cacheFolder;
    }

    public static void setCacheFolder(File cacheFolder) {
        JavaShell.cacheFolder = cacheFolder;
    }

    public static File getLibFolder() {
        return libFolder;
    }

    public static void setLibFolder(File libFolder) {
        JavaShell.libFolder = libFolder;
    }

    public static HashMap<String, Class<?>> getShells() {
        return shells;
    }

    public static void setShells(HashMap<String, Class<?>> shells) {
        JavaShell.shells = shells;
    }

    public static void javaShellSetup() {
        File dataFolder = Main.getInst().getDataFolder();
        File pluginsFolder = dataFolder.getParentFile();
        File serverRoot = Bukkit.getWorldContainer();

        File[] rootJars = serverRoot.listFiles((dir, name) -> name.toLowerCase().endsWith("jar"));
        File[] pluginJars = pluginsFolder.listFiles((dir, name) -> name.toLowerCase().endsWith("jar"));

        for (File file : (File[]) ArrayUtils.addAll(rootJars, pluginJars)) {
            String path = file.getAbsolutePath();
            paths += File.pathSeparator + path;
        }

        javaShellFolder = new File(Main.getInst().getDataFolder(), "JavaShells");
        if (!javaShellFolder.exists()) {
            Main.getInst().saveResource("JavaShells/scripts/-testshell.java", true);
        }

        scriptFolder = new File(javaShellFolder, "scripts");
        if (!scriptFolder.exists()) {
            scriptFolder.mkdir();
        }

        cacheFolder = new File(javaShellFolder, "cache");
        if (!cacheFolder.exists()) {
            cacheFolder.mkdir();
        }

        libFolder = new File(javaShellFolder, "lib");
        if (!libFolder.exists()) {
            libFolder.mkdir();
        }

        File tools = new File(Main.getInst().getDataFolder(), "JavaShells/lib/com.sun.tools.jar");
        if (!tools.exists()) {
            MsgUtils.warn("&4JavaShell &c工具的必要依赖 &4com.sun.tools.jar &c不存在, 功能关闭!");
            return;
        }

        loadLibrary();

        new BukkitRunnable() {

            @Override
            public void run() {
                long time = System.currentTimeMillis();
                Arrays.stream(Objects.requireNonNull(scriptFolder.listFiles())).filter(file -> !file.getName().startsWith("-")).map(File::getName).forEach(JavaShell::reloadShell);
                MsgUtils.send("载入 " + shells.size() + " 个脚本, 耗时 &f" + (System.currentTimeMillis() - time) + "ms");
            }
        }.runTask(Main.getInst());
    }

    public static void javaShellCancel() {
        try {
            Arrays.stream(Objects.requireNonNull(cacheFolder.listFiles())).forEach(File::delete);
            shells.keySet().forEach(name -> invokeMethod(name, "onDisable"));
        } catch (Exception ignored) {
        }
    }

    public static void invokeMethod(String name, String method) {
        if (shells.containsKey(name)) {
            Class<?> clazz = shells.get(name);
            try {
                Method disableMethod = clazz.getMethod(method);
                if (disableMethod != null) {
                    disableMethod.invoke(clazz.newInstance());
                }
            } catch (Exception ignored) {
            }
        }
    }

    public static void unloadShell(String shell) {
        invokeMethod(shell, "onDisable");
        Class<?> clazz = shells.remove(shell);
        try {
            if (clazz.newInstance() instanceof Listener) {
                HandlerList.getRegisteredListeners(Main.getInst()).stream().filter(listener -> listener.getListener().getClass().getName().equals(clazz.getName())).map(RegisteredListener::getListener).forEach(HandlerList::unregisterAll);
                MsgUtils.send("已为脚本 &f" + shell + " &7注销监听器");
            }
        } catch (Exception ignored) {
        }
    }

    public static boolean reloadShell(String shell) {
        unloadShell(shell = shell.replace(".java", ""));

        try {
            Class.forName("com.sun.tools.javac.main.Main");
        } catch (Exception e) {
            MsgUtils.warn("&4JavaShell &c工具的必要依赖 &4com.sun.tools.jar &c丢失, 无法载入!");
            return false;
        }

        File javaFile = new File(scriptFolder, shell + ".java");
        if (!javaFile.exists()) {
            MsgUtils.send("&c脚本 &4" + shell + "&c 不存在");
            return false;
        }

        String[] args = {
                "-nowarn",
                "-classpath", "." + File.pathSeparator + JavaShell.getPaths(),
                "-d", cacheFolder.getAbsolutePath() + File.separator,
                javaFile.getAbsolutePath()
        };

        int code = new com.sun.tools.javac.main.Main("javac").compile(args).exitCode;
        if (code == 0) {
            MsgUtils.send("&f" + shell + "&7 载入成功");
            try {
                URL[] urls = {cacheFolder.toURI().toURL()};
                URLClassLoader sysloader = new URLClassLoader(urls, Main.class.getClassLoader());
                Class<?> clazz = sysloader.loadClass(shell);
                shells.put(shell, clazz);
                sysloader.close();

                invokeMethod(shell, "onEnable");
                if (clazz.newInstance() instanceof Listener) {
                    Bukkit.getPluginManager().registerEvents((Listener) clazz.newInstance(), Main.getInst());
                    MsgUtils.send("已为脚本 &f" + shell + " &7注册监听器");
                }
            } catch (Exception e) {
                //
            }
            return true;
        } else {
            MsgUtils.send("&4" + shell + "&c 载入失败");
            return false;
        }
    }

    private static void loadLibrary() {
        Arrays.stream(Objects.requireNonNull(libFolder.listFiles())).forEach(jar -> TDependencyLoader.addToPath(Main.getInst(), jar));
    }
}
