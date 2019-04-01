package me.skymc.taboolib;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ilummc.tlib.TLib;
import com.ilummc.tlib.annotations.Dependency;
import com.ilummc.tlib.dependency.TDependencyLoader;
import com.ilummc.tlib.inject.TDependencyInjector;
import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.bstats.Metrics;
import me.skymc.taboolib.deprecated.TabooLibDeprecated;
import me.skymc.taboolib.events.TPluginEnableEvent;
import me.skymc.taboolib.fileutils.FileUtils;
import me.skymc.taboolib.listener.TListener;
import me.skymc.taboolib.listener.TListenerHandler;
import me.skymc.taboolib.methods.ReflectionUtils;
import me.skymc.taboolib.playerdata.DataUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.InetAddress;
import java.util.*;

/**
 * @Author sky
 * @Since 2018-08-23 17:04
 */
@TListener
public class TabooLibLoader implements Listener {

    /*
        关于 TabooLib 各项自动化接口的执行顺序

         [ENABLING]
         第一阶段：运行 @TInject（Instance Inject）
         第二阶段（先后）：实例化 @TListener -> 实例化 @Instantiable
         第三阶段（并行）：运行 @TFunction & 运行 @TInject

         [ENABLED]
         第三阶段：注册 @TListener
     */

    static TabooLibDeprecated tabooLibDeprecated;
    static Map<String, List<Class>> pluginClasses = Maps.newHashMap();
    static List<Loader> loaders = Lists.newArrayList();
    static List<Runnable> tasks = Lists.newArrayList();
    static boolean started;

    static void setup() {
        testInternet();
        setupDataFolder();
        setupDatabase();
        setupLibraries();
    }

    static void register() {
        setupClasses();
        preLoadClasses();
        registerListener();
        registerMetrics();
        postLoadClasses();
        try {
            tabooLibDeprecated = new TabooLibDeprecated();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Bukkit.getScheduler().runTask(TabooLib.instance(), () -> {
            for (Runnable task : tasks) {
                try {
                    task.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    static void unregister() {
        unloadClasses();
        try {
            tabooLibDeprecated.unregister();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static TabooLibDeprecated getTabooLibDeprecated() {
        return tabooLibDeprecated;
    }

    public static Optional<List<Class>> getPluginClasses(Plugin plugin) {
        return Optional.ofNullable(pluginClasses.get(plugin.getName()));
    }

    public static List<Class> getPluginClassSafely(Plugin plugin) {
        List<Class> classes = pluginClasses.get(plugin.getName());
        return classes == null ? new ArrayList<>() : new ArrayList<>(classes);
    }

    public static void runTaskOnEnabled(Runnable runnable) {
        if (Main.isStarted()) {
            Bukkit.getScheduler().runTask(TabooLib.instance(), () -> {
                try {
                    runnable.run();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            });
        } else {
            tasks.add(runnable);
        }
    }

    static boolean isLoader(Class pluginClass) {
        return !Loader.class.equals(pluginClass) && Loader.class.isAssignableFrom(pluginClass);
    }

    static void preLoadClasses() {
        pluginClasses.forEach((key, classes) -> classes.forEach(pluginClass -> preLoadClass(Bukkit.getPluginManager().getPlugin(key), pluginClass)));
    }

    static void postLoadClasses() {
        pluginClasses.forEach((key, classes) -> classes.forEach(pluginClass -> postLoadClass(Bukkit.getPluginManager().getPlugin(key), pluginClass)));
    }

    static void unloadClasses() {
        pluginClasses.forEach((key, classes) -> classes.forEach(pluginClass -> unloadClass(Bukkit.getPluginManager().getPlugin(key), pluginClass)));
    }

    static void registerListener() {
        TListenerHandler.setupListeners();
        Bukkit.getScheduler().runTask(TabooLib.instance(), TListenerHandler::registerListeners);
    }

    static void registerMetrics() {
        Metrics metrics = new Metrics(TabooLib.instance());
        metrics.addCustomChart(new Metrics.SingleLineChart("plugins_using_taboolib", () -> Math.toIntExact(Arrays.stream(Bukkit.getPluginManager().getPlugins()).filter(plugin -> plugin.getDescription().getDepend().contains("TabooLib")).count())));
    }

    static void setupDataFolder() {
        Main.setPlayerDataFolder(FileUtils.folder(Main.getInst().getConfig().getString("DATAURL.PLAYER-DATA")));
        Main.setServerDataFolder(FileUtils.folder(Main.getInst().getConfig().getString("DATAURL.SERVER-DATA")));
    }

    static void setupDatabase() {
        DataUtils.addPluginData("TabooLibrary", null);
        Main.setStorageType(Main.StorageType.LOCAL);
//        Main.setStorageType(Main.getInst().getConfig().getBoolean("MYSQL.ENABLE") ? Main.StorageType.SQL : Main.StorageType.LOCAL);
//        TabooLibDatabase.init();
    }

    static void setupAddons() {
        TabooLib.instance().saveResource("Addons/TabooLibDeprecated", true);
        // 傻逼 Gradle 的 shadow 插件会将所有 jar 排除
        // https://github.com/johnrengelman/shadow/issues/276
        File from = new File(TabooLib.instance().getDataFolder(), "Addons/TabooLibDeprecated");
        from.renameTo(new File(TabooLib.instance().getDataFolder(), "Addons/TabooLibDeprecated.jar"));
        from.delete();
        File file = new File(TabooLib.instance().getDataFolder(), "Addons");
        if (file.exists()) {
            Arrays.stream(file.listFiles()).forEach(listFile -> TDependencyLoader.addToPath(TabooLib.instance(), listFile));
        }
    }

    static void setupLibraries() {
        if (Main.isOfflineVersion()) {
            Arrays.stream(TDependencyInjector.getDependencies(TLib.getTLib())).filter(dependency -> dependency.type() == Dependency.Type.LIBRARY && dependency.maven().matches(".*:.*:.*")).map(dependency -> String.join("-", dependency.maven().split(":")) + ".jar").forEach(fileName -> {
                File targetFile = FileUtils.file(TLib.getTLib().getLibsFolder(), fileName);
                InputStream inputStream = FileUtils.getResource("libs/" + fileName);
                if (!targetFile.exists() && inputStream != null) {
                    FileUtils.inputStreamToFile(inputStream, FileUtils.file(TLib.getTLib().getLibsFolder(), fileName));
                }
            });
        }
    }

    static void testInternet() {
        try {
            InetAddress inetAddress = InetAddress.getByName(Main.getInst().getConfig().getString("TEST-URL", "aliyun.com"));
            Main.setIsInternetOnline(inetAddress.isReachable(10000));
        } catch (Exception ignored) {
        }
        if (!Main.isInternetOnline() && !Main.isOfflineVersion() && !Main.isLibrariesExists()) {
            TLocale.Logger.error("TLIB.LOAD-FAIL-OFFLINE", Main.getInst().getDescription().getVersion());
            for (; ; ) {
                // 停止主线程
            }
        }
    }

    static void setupClasses(Plugin plugin) {
        if (TabooLib.isTabooLib(plugin) || TabooLib.isDependTabooLib(plugin)) {
            try {
                long time = System.currentTimeMillis();
                List<Class> classes;
                IgnoreClasses annotation = plugin.getClass().getAnnotation(IgnoreClasses.class);
                if (annotation != null) {
                    classes = FileUtils.getClasses(plugin, annotation.value());
                } else {
                    classes = FileUtils.getClasses(plugin);
                }
                TabooLib.debug("Saved " + classes.size() + " classes (" + plugin.getName() + ") (" + (System.currentTimeMillis() - time) + "ms)");
                pluginClasses.put(plugin.getName(), classes);
            } catch (Exception ignored) {
            }
        }
    }

    static void setupClasses() {
        Arrays.stream(Bukkit.getPluginManager().getPlugins()).forEach(TabooLibLoader::setupClasses);
        pluginClasses.get("TabooLib").stream().filter(TabooLibLoader::isLoader).forEach(pluginClass -> {
            try {
                loaders.add((Loader) ReflectionUtils.instantiateObject(pluginClass));
            } catch (Exception e) {
                e.printStackTrace();
            }
            loaders.sort(Comparator.comparingInt(Loader::priority));
        });
    }

    static void preLoadClass(Plugin plugin, Class<?> loadClass) {
        loaders.forEach(loader -> {
            try {
                loader.preLoad(plugin, loadClass);
            } catch (NoClassDefFoundError ignore) {
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
    }

    static void postLoadClass(Plugin plugin, Class<?> loadClass) {
        loaders.forEach(loader -> {
            try {
                loader.postLoad(plugin, loadClass);
            } catch (NoClassDefFoundError ignore) {
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
    }

    static void unloadClass(Plugin plugin, Class<?> loadClass) {
        loaders.forEach(loader -> {
            try {
                loader.unload(plugin, loadClass);
            } catch (NoClassDefFoundError ignore) {
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEnablePre(TPluginEnableEvent e) {
        setupClasses(e.getPlugin());
        Optional.ofNullable(pluginClasses.get(e.getPlugin().getName())).ifPresent(classes -> classes.forEach(pluginClass -> preLoadClass(e.getPlugin(), pluginClass)));
    }

    @EventHandler
    public void onEnablePost(TPluginEnableEvent e) {
        Optional.ofNullable(pluginClasses.get(e.getPlugin().getName())).ifPresent(classes -> classes.forEach(pluginClass -> postLoadClass(e.getPlugin(), pluginClass)));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDisable(PluginDisableEvent e) {
        Optional.ofNullable(pluginClasses.remove(e.getPlugin().getName())).ifPresent(classes -> classes.forEach(pluginClass -> unloadClass(e.getPlugin(), pluginClass)));
    }

    public interface Loader {

        default void preLoad(Plugin plugin, Class<?> loadClass) {
        }

        default void postLoad(Plugin plugin, Class<?> loadClass) {
        }

        default void unload(Plugin plugin, Class<?> cancelClass) {
        }

        default int priority() {
            return 0;
        }
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface IgnoreClasses {

        String[] value();

    }
}
