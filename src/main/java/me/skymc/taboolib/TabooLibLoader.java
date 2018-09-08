package me.skymc.taboolib;

import com.ilummc.tlib.TLib;
import com.ilummc.tlib.annotations.Dependency;
import com.ilummc.tlib.inject.TDependencyInjector;
import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.bstats.Metrics;
import me.skymc.taboolib.fileutils.FileUtils;
import me.skymc.taboolib.listener.TListener;
import me.skymc.taboolib.listener.TListenerHandler;
import me.skymc.taboolib.playerdata.DataUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @Author sky
 * @Since 2018-08-23 17:04
 */
@TListener
public class TabooLibLoader implements Listener {

    static HashMap<String, List<Class>> pluginClasses = new HashMap<>();

    @EventHandler (priority = EventPriority.LOWEST)
    public void onEnable(PluginEnableEvent e) {
        pluginClasses.remove(e.getPlugin().getName());
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onDisable(PluginDisableEvent e) {
        setupClasses(e.getPlugin());
    }

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
    }

    /**
     * 获取插件所有被读取到的类
     *
     * @param plugin 插件
     * @return List
     */
    public static Optional<List<Class>> getPluginClasses(Plugin plugin) {
        return Optional.ofNullable(pluginClasses.get(plugin.getName()));
    }

    /**
     * 初始化插件文件夹
     */
    static void setupDataFolder() {
        Main.setPlayerDataFolder(FileUtils.folder(Main.getInst().getConfig().getString("DATAURL.PLAYER-DATA")));
        Main.setServerDataFolder(FileUtils.folder(Main.getInst().getConfig().getString("DATAURL.SERVER-DATA")));
    }

    /**
     * 载入插件数据库
     */
    static void setupDatabase() {
        DataUtils.addPluginData("TabooLibrary", null);
        // 检查是否启用数据库
        Main.setStorageType(Main.getInst().getConfig().getBoolean("MYSQL.ENABLE") ? Main.StorageType.SQL : Main.StorageType.LOCAL);
        // 初始化数据库
        TabooLibDatabase.init();
    }

    /**
     * 读取插件类
     */
    static void setupClasses() {
        Arrays.stream(Bukkit.getPluginManager().getPlugins()).forEach(TabooLibLoader::setupClasses);
    }

    /**
     * 读取插件类
     */
    static void setupClasses(Plugin plugin) {
        if (!(TabooLib.isTabooLib(plugin) || TabooLib.isDependTabooLib(plugin))) {
            return;
        }
        try {
            long time = System.currentTimeMillis();
            List<Class> classes = FileUtils.getClasses(plugin);
            TLocale.Logger.info("DEPENDENCY.LOAD-CLASSES", plugin.getName(), String.valueOf(classes.size()), String.valueOf(System.currentTimeMillis() - time));
            pluginClasses.put(plugin.getName(), classes);
        } catch (Exception ignored) {
        }
    }

    /**
     * 初始化插件依赖库
     */
    static void setupLibraries() {
        if (!Main.isOfflineVersion()) {
            return;
        }
        for (Dependency dependency : TDependencyInjector.getDependencies(TLib.getTLib())) {
            if (dependency.type() == Dependency.Type.LIBRARY && dependency.maven().matches(".*:.*:.*")) {
                String fileName = String.join("-", dependency.maven().split(":")) + ".jar";
                File targetFile = FileUtils.file(TLib.getTLib().getLibsFolder(), fileName);
                InputStream inputStream = FileUtils.getResource("libs/" + fileName);
                if (!targetFile.exists() && inputStream != null) {
                    FileUtils.inputStreamToFile(inputStream, FileUtils.file(TLib.getTLib().getLibsFolder(), fileName));
                }
            }
        }
    }

    /**
     * 检查网络连接状态
     */
    static void testInternet() {
        try {
            InetAddress inetAddress = InetAddress.getByName(Main.getInst().getConfig().getString("TEST-URL", "aliyun.com"));
            Main.setIsInternetOnline(inetAddress.isReachable(10000));
        } catch (Exception ignored) {
        }
        if (!Main.isInternetOnline() && !Main.isOfflineVersion() && !Main.isLibrariesExists()) {
            TLocale.Logger.error("TLIB.LOAD-FAIL-OFFLINE", Main.getInst().getDescription().getVersion());
            try {
                while (true) {
                    Thread.sleep(1000);
                }
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 载入插件监听
     */
    static void registerListener() {
        // 载入所有 TListener 监听器
        TListenerHandler.setupListeners();
        // 注册所有 TListener 监听器
        Bukkit.getScheduler().runTask(TabooLib.instance(), TListenerHandler::registerListeners);
    }

    /**
     * 注册插件统计
     */
    static void registerMetrics() {
        Metrics metrics = new Metrics(TabooLib.instance());
        metrics.addCustomChart(new Metrics.SingleLineChart("plugins_using_taboolib", () -> Math.toIntExact(Arrays.stream(Bukkit.getPluginManager().getPlugins()).filter(plugin -> plugin.getDescription().getDepend().contains("TabooLib")).count())));
        metrics.addCustomChart(new Metrics.AdvancedPie("plugins_using_taboolib_name", () -> Arrays.stream(Bukkit.getPluginManager().getPlugins()).filter(plugin -> plugin.getDescription().getDepend().contains("TabooLib")).collect(Collectors.toMap(Plugin::getName, plugin -> 1, (a, b) -> b))));
    }
}
