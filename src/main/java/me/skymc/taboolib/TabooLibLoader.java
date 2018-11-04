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
import me.skymc.taboolib.events.TPluginLoadEvent;
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
import java.net.InetAddress;
import java.util.*;

/**
 * @Author sky
 * @Since 2018-08-23 17:04
 */
@TListener
public class TabooLibLoader implements Listener {

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
        registerListener();
        registerMetrics();
        loadClasses();
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
            runnable.run();
        } else {
            tasks.add(runnable);
        }
    }

    static boolean isLoader(Class pluginClass) {
        return !Loader.class.equals(pluginClass) && Loader.class.isAssignableFrom(pluginClass);
    }

    static void loadClasses() {
        pluginClasses.forEach((key, classes) -> classes.forEach(pluginClass -> loadClass(Bukkit.getPluginManager().getPlugin(key), pluginClass)));
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
        Main.setStorageType(Main.getInst().getConfig().getBoolean("MYSQL.ENABLE") ? Main.StorageType.SQL : Main.StorageType.LOCAL);
        TabooLibDatabase.init();
    }

    static void setupAddons() {
        TabooLib.instance().saveResource("Addons/TabooLibDeprecated.jar", true);
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
                List<Class> classes = FileUtils.getClasses(plugin);
                TLocale.Logger.info("DEPENDENCY.LOAD-CLASSES", plugin.getName(), String.valueOf(classes.size()), String.valueOf(System.currentTimeMillis() - time));
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
        });
    }

    static void loadClass(Plugin plugin, Class<?> loadClass) {
        loaders.forEach(loader -> {
            try {
                loader.load(plugin, loadClass);
            } catch (Throwable ignored) {
            }
        });
    }

    static void unloadClass(Plugin plugin, Class<?> loadClass) {
        loaders.forEach(loader -> {
            try {
                loader.unload(plugin, loadClass);
            } catch (Throwable ignored) {
            }
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEnable(TPluginEnableEvent e) {
        setupClasses(e.getPlugin());
        Optional.ofNullable(pluginClasses.get(e.getPlugin().getName())).ifPresent(classes -> classes.forEach(pluginClass -> loadClass(e.getPlugin(), pluginClass)));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDisable(PluginDisableEvent e) {
        Optional.ofNullable(pluginClasses.remove(e.getPlugin().getName())).ifPresent(classes -> classes.forEach(pluginClass -> unloadClass(e.getPlugin(), pluginClass)));
    }

    public interface Loader {

        default void load(Plugin plugin, Class<?> loadClass) {
        }

        default void unload(Plugin plugin, Class<?> cancelClass) {
        }
    }
}
