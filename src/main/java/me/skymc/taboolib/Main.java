package me.skymc.taboolib;

import com.ilummc.tlib.TLib;
import com.ilummc.tlib.annotations.Dependency;
import com.ilummc.tlib.inject.TDependencyInjector;
import com.ilummc.tlib.resources.TLocale;
import com.ilummc.tlib.util.IO;
import com.ilummc.tlib.util.Strings;
import me.skymc.taboolib.anvil.AnvilContainerAPI;
import me.skymc.taboolib.bstats.Metrics;
import me.skymc.taboolib.commands.TabooLibMainCommand;
import me.skymc.taboolib.commands.internal.TBaseCommand;
import me.skymc.taboolib.commands.language.Language2Command;
import me.skymc.taboolib.commands.locale.TabooLibLocaleCommand;
import me.skymc.taboolib.commands.plugin.TabooLibPluginMainCommand;
import me.skymc.taboolib.commands.taboolib.listener.ListenerItemListCommand;
import me.skymc.taboolib.commands.taboolib.listener.ListenerSoundsCommand;
import me.skymc.taboolib.database.GlobalDataManager;
import me.skymc.taboolib.database.PlayerDataManager;
import me.skymc.taboolib.economy.EcoUtils;
import me.skymc.taboolib.entity.EntityUtils;
import me.skymc.taboolib.fileutils.ConfigUtils;
import me.skymc.taboolib.fileutils.FileUtils;
import me.skymc.taboolib.inventory.ItemUtils;
import me.skymc.taboolib.inventory.speciaitem.SpecialItem;
import me.skymc.taboolib.itagapi.TagDataHandler;
import me.skymc.taboolib.javashell.JavaShell;
import me.skymc.taboolib.listener.*;
import me.skymc.taboolib.message.ChatCatcher;
import me.skymc.taboolib.mysql.hikari.HikariHandler;
import me.skymc.taboolib.mysql.protect.MySQLConnection;
import me.skymc.taboolib.nms.item.DabItemUtils;
import me.skymc.taboolib.other.NumberUtils;
import me.skymc.taboolib.permission.PermissionUtils;
import me.skymc.taboolib.playerdata.DataUtils;
import me.skymc.taboolib.sign.SignUtils;
import me.skymc.taboolib.skript.SkriptHandler;
import me.skymc.taboolib.string.StringUtils;
import me.skymc.taboolib.string.language2.Language2;
import me.skymc.taboolib.support.SupportPlaceholder;
import me.skymc.taboolib.timecycle.TimeCycleManager;
import me.skymc.taboolib.update.UpdateTask;
import me.skymc.tlm.TLM;
import me.skymc.tlm.command.TLMCommands;
import me.skymc.tlm.module.TabooLibraryModule;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Random;

/**
 * @author sky
 */
public class Main extends JavaPlugin implements Listener {

    public Main() {
        inst = this;
    }

    public enum StorageType {
        LOCAL, SQL
    }

    private static Plugin inst;

    private static net.milkbowl.vault.economy.Economy Economy;

    private static File playerDataFolder;

    private static File serverDataFolder;

    private static StorageType storageType = StorageType.LOCAL;

    private static boolean disable = false;

    private static MySQLConnection connection = null;

    private static Language2 exampleLanguage2;

    private static boolean started;

    private static boolean isInternetOnline = false;

    private FileConfiguration config = null;

    @Override
    public FileConfiguration getConfig() {
        return config;
    }

    @Override
    public void saveDefaultConfig() {
        reloadConfig();
    }

    @Override
    public void reloadConfig() {
        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            saveResource("config.yml", true);
        }
        config = ConfigUtils.load(this, file);
    }

    @Override
    public void onLoad() {
        disable = false;
        // 载入配置
        saveDefaultConfig();
        // 载入牛逼玩意儿
        TLib.init();
        TLib.injectPluginManager();
        // 网络检测
        testInternet();
        // 创建文件夹
        setupDataFolder();
        // 创建数据库
        setupDatabase();
        // 载入离线库文件
        setupLibraries();
        // 载入牛逼玩意儿
        TLib.initPost();
    }

    @Override
    public void onEnable() {
        // 注册命令
        registerCommands();
        // 注册监听
        registerListener();

        // 载入经济
        EcoUtils.setupEconomy();
        // 载入权限
        PermissionUtils.loadRegisteredServiceProvider();
        // 物品名称
        ItemUtils.LoadLib();
        // 低层工具
        DabItemUtils.getInstance();
        // 载入周期管理器
        TimeCycleManager.load();
        // 启动脚本
        JavaShell.javaShellSetup();
        // 注册脚本
        SkriptHandler.getInst();
        // 注册头衔
        TagDataHandler.init(this);
        // 载入语言文件
        exampleLanguage2 = new Language2("Language2", this);

        // 启动数据库储存方法
        if (getStorageType() == StorageType.SQL) {
            GlobalDataManager.SQLMethod.startSQLMethod();
        }

        // 载入完成
        TLocale.Logger.info("NOTIFY.SUCCESS-LOADED", getDescription().getAuthors().toString(), getDescription().getVersion(), String.valueOf(TabooLib.getVersion()));

        // 文件保存
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, DataUtils::saveAllCaches, 20, 20 * 120);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> PlayerDataManager.saveAllCaches(true, false), 20, 20 * 60);

        // 文件监控
        TLib.getTLib().getConfigWatcher().addListener(new File(getDataFolder(), "config.yml"), null, obj -> {
            reloadConfig();
            TLocale.Logger.info("CONFIG.RELOAD-SUCCESS", inst.getName(), "config.yml");
        });

        // 插件联动
        new BukkitRunnable() {

            @Override
            public void run() {
                // 载入 PlaceholderAPI 扩展
                if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                    new SupportPlaceholder(getInst(), "taboolib").hook();
                }

                // 载入 SpecialItem 接口
                SpecialItem.getInst().loadItems();
                // 载入 TLM 接口
                TLM.getInst();

                // 面子工程
                InputStream inputStream = FileUtils.getResource("motd.txt");
                try {
                    String text = new String(IO.readFully(inputStream), Charset.forName("utf-8"));
                    if (text != null) {
                        Arrays.stream(text.split("\n")).forEach(line -> Bukkit.getConsoleSender().sendMessage(Strings.replaceWithOrder(line, getDescription().getVersion())));
                    }
                } catch (IOException ignored) {
                }
            }
        }.runTask(this);

        // 更新检测
        new UpdateTask();
        // 启动监控
        new Metrics(this);

        // 启动
        started = true;
    }

    @Override
    public void onDisable() {
        disable = true;

        // 如果插件尚未启动完成
        if (!started) {
            TLocale.Logger.error("NOTIFY.FAIL-DISABLE");
            return;
        }

        // 保存数据
        Bukkit.getOnlinePlayers().forEach(x -> DataUtils.saveOnline(x.getName()));
        // 结束线程
        Bukkit.getScheduler().cancelTasks(this);
        // 保存插件数据
        DataUtils.saveAllCaches();
        // 保存玩家数据
        PlayerDataManager.saveAllPlayers(false, true);
        // 结束脚本
        JavaShell.javaShellCancel();
        // 注销 SpecialItem 接口
        SpecialItem.getInst().unloadItems();
        // 注销 TLM 接口
        TabooLibraryModule.getInst().unloadModules();
        // 注销连接池
        HikariHandler.closeDataSourceForce();

        // 结束数据库储存方法
        if (getStorageType() == StorageType.SQL) {
            GlobalDataManager.SQLMethod.cancelSQLMethod();
        }

        // 清理数据
        if (getStorageType() == StorageType.LOCAL && getConfig().getBoolean("DELETE-DATA")) {
            getPlayerDataFolder().delete();
        }
        // 清理数据
        if (getStorageType() == StorageType.SQL && getConfig().getBoolean("DELETE-VARIABLE")) {
            GlobalDataManager.clearInvalidVariables();
        }

        // 提示信息
        TLocale.Logger.error("NOTIFY.SUCCESS-DISABLE");

        // 结束连接
        if (connection != null && connection.isConnection()) {
            connection.closeConnection();
        }

        // 卸载牛逼玩意儿
        TLib.unload();

        // 关闭服务器
        Bukkit.shutdown();
    }

    private void testInternet() {
        try {
            InetAddress inetAddress = InetAddress.getByName(getConfig().getString("TEST-URL", "aliyun.com"));
            isInternetOnline = inetAddress.isReachable(10000);
        } catch (Exception ignored) {
        }
        if (!isInternetOnline() && !isOfflineVersion() && !isLibrariesExists()) {
            TLocale.Logger.error("TLIB.LOAD-FAIL-OFFLINE", getDescription().getVersion());
            // 死锁
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (Exception ignored) {
            }
        }
    }

    private void setupDataFolder() {
        playerDataFolder = new File(getConfig().getString("DATAURL.PLAYER-DATA"));
        if (!playerDataFolder.exists()) {
            playerDataFolder.mkdirs();
        }
        serverDataFolder = new File(getConfig().getString("DATAURL.SERVER-DATA"));
        if (!serverDataFolder.exists()) {
            serverDataFolder.mkdirs();
        }
    }

    private void setupLibraries() {
        if (!isOfflineVersion()) {
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

    private void setupDatabase() {
        DataUtils.addPluginData("TabooLibrary", null);
        if (getConfig().getBoolean("MYSQL.ENABLE")) {
            connection = new MySQLConnection(getConfig().getString("MYSQL.HOST"), getConfig().getString("MYSQL.USER"), getConfig().getString("MYSQL.POST"), getConfig().getString("MYSQL.PASSWORD"), getConfig().getString("MYSQL.DATABASE"), 30, this);
            if (connection.isConnection()) {
                connection.createTable(getTablePrefix() + "_playerdata", "username", "configuration");
                connection.createTable(getTablePrefix() + "_plugindata", "name", "variable", "upgrade");
                connection.createTable(getTablePrefix() + "_serveruuid", "uuid", "hash");
                if (!connection.isExists(getTablePrefix() + "_serveruuid", "uuid", TabooLib.getServerUID())) {
                    connection.intoValue(getTablePrefix() + "_serveruuid", TabooLib.getServerUID(), StringUtils.hashKeyForDisk(getDataFolder().getPath()));
                } else {
                    String hash = connection.getValue(getTablePrefix() + "_serveruuid", "uuid", TabooLib.getServerUID(), "hash").toString();
                    if (!hash.equals(StringUtils.hashKeyForDisk(getDataFolder().getPath()))) {
                        TLocale.Logger.error("NOTIFY.ERROR-SERVER-KEY");
                        TabooLib.resetServerUID();
                        Bukkit.shutdown();
                    }
                }
            } else {
                TLocale.Logger.error("NOTIFY.ERROR-CONNECTION-FAIL");
                Bukkit.shutdown();
            }
            storageType = StorageType.SQL;
        }
    }

    private void registerCommands() {
        getCommand("language2").setExecutor(new Language2Command());
        getCommand("taboolibrarymodule").setExecutor(new TLMCommands());
        TBaseCommand.registerCommand("taboolib", new TabooLibMainCommand());
        TBaseCommand.registerCommand("tabooliblocale", new TabooLibLocaleCommand());
        TBaseCommand.registerCommand("taboolibplugin", new TabooLibPluginMainCommand());
    }

    private void registerListener() {
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new ListenerPlayerCommand(), this);
        getServer().getPluginManager().registerEvents(new ListenerPlayerJump(), this);
        getServer().getPluginManager().registerEvents(new ListenerPlayerJoinAndQuit(), this);
        getServer().getPluginManager().registerEvents(new ChatCatcher(), this);
        getServer().getPluginManager().registerEvents(new DataUtils(), this);
        getServer().getPluginManager().registerEvents(new AnvilContainerAPI(), this);
        getServer().getPluginManager().registerEvents(new ListenerPluginDisable(), this);
        getServer().getPluginManager().registerEvents(new PlayerDataManager(), this);
        getServer().getPluginManager().registerEvents(new ListenerItemListCommand(), this);
        getServer().getPluginManager().registerEvents(new ListenerSoundsCommand(), this);

        if (TabooLib.getVerint() > 10700) {
            getServer().getPluginManager().registerEvents(new EntityUtils(), this);
            getServer().getPluginManager().registerEvents(new SignUtils(), this);
        }

        if (Bukkit.getPluginManager().getPlugin("YUM") != null) {
            getServer().getPluginManager().registerEvents(new ListenerNetWork(), this);
        }
    }

    // *********************************
    //
    //        Getter and Setter
    //
    // *********************************

    public static Plugin getInst() {
        return inst;
    }

    public static String getPrefix() {
        return "§8[§3§lTabooLib§8] §7";
    }

    public static net.milkbowl.vault.economy.Economy getEconomy() {
        return Economy;
    }

    public static void setEconomy(net.milkbowl.vault.economy.Economy economy) {
        Economy = economy;
    }

    public static File getPlayerDataFolder() {
        return playerDataFolder;
    }

    public static File getServerDataFolder() {
        return serverDataFolder;
    }

    public static StorageType getStorageType() {
        return storageType;
    }

    public static boolean isDisable() {
        return disable;
    }

    public static MySQLConnection getConnection() {
        return connection;
    }

    public static Language2 getExampleLanguage2() {
        return exampleLanguage2;
    }

    public static boolean isStarted() {
        return started;
    }

    public static Random getRandom() {
        return NumberUtils.getRandom();
    }

    public static String getTablePrefix() {
        return inst.getConfig().getString("MYSQL.PREFIX");
    }

    public static boolean isInternetOnline() {
        return isInternetOnline;
    }

    public static boolean isOfflineVersion() {
        return inst.getResource("libs") != null;
    }

    public static boolean isLibrariesExists() {
        return TLib.getTLib().getLibsFolder().listFiles().length > 0;
    }
}
