package me.skymc.taboolib;

import com.ilummc.tlib.TLib;
import com.ilummc.tlib.annotations.Dependency;
import com.ilummc.tlib.inject.TDependencyInjector;
import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.bstats.Metrics;
import me.skymc.taboolib.commands.language.Language2Command;
import me.skymc.taboolib.fileutils.FileUtils;
import me.skymc.taboolib.listener.TListenerHandler;
import me.skymc.taboolib.playerdata.DataUtils;
import me.skymc.tlm.command.TLMCommands;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @Author sky
 * @Since 2018-08-23 17:04
 */
class TabooLibSettings {

    static void setup() {
        testInternet();
        setupDataFolder();
        setupDatabase();
        setupLibraries();
    }

    static void register() {
        registerCommands();
        registerListener();
        registerMetrics();
    }

    /**
     * 初始化插件文件夹
     */
    static void setupDataFolder() {
        Main.setPlayerDataFolder(FileUtils.folder(Main.getInst().getConfig().getString("DATAURL.PLAYER-DATA")));
        Main.setServerDataFolder(FileUtils.folder(Main.getInst().getConfig().getString("DATAURL.SERVER-DATA")));
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
     * 载入插件命令
     */
    static void registerCommands() {
        Bukkit.getPluginCommand("language2").setExecutor(new Language2Command());
        Bukkit.getPluginCommand("taboolibrarymodule").setExecutor(new TLMCommands());
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
