package io.izzel.taboolib;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.izzel.taboolib.common.loader.StartupLoader;
import io.izzel.taboolib.common.plugin.InternalPlugin;
import io.izzel.taboolib.module.command.TCommandHandler;
import io.izzel.taboolib.module.config.TConfig;
import io.izzel.taboolib.module.config.TConfigWatcher;
import io.izzel.taboolib.module.db.local.Local;
import io.izzel.taboolib.module.db.source.DBSource;
import io.izzel.taboolib.module.dependency.TDependencyInjector;
import io.izzel.taboolib.module.inject.TListenerHandler;
import io.izzel.taboolib.module.inject.TScheduleLoader;
import io.izzel.taboolib.module.locale.TLocaleLoader;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @Author 坏黑
 * @Since 2019-07-05 15:14
 */
public abstract class PluginLoader {

    private static final List<PluginLoader> registerLoader = Lists.newArrayList();
    private static final Map<String, Object> redefine = Maps.newHashMap();
    private static final Set<String> plugins = Sets.newHashSet();
    private static boolean firstLoading = false;
    private static boolean firstStarting = false;
    private static Plugin firstLoaded = null;

    static {
        registerLoader.add(new PluginLoader() {

            @Override
            public void onLoading(Plugin plugin) {
                // 注入依赖
                TDependencyInjector.inject(plugin, plugin.getClass());
                // 加载语言文件
                TLocaleLoader.load(plugin, false);
                // 读取插件类
                TabooLibLoader.setupClasses(plugin);
                // 加载插件类
                TabooLibLoader.preLoadClass(plugin, TabooLibLoader.getPluginClassSafely(plugin));
                // 首次运行
                if (!firstLoading && !(plugin instanceof InternalPlugin)) {
                    firstLoaded = plugin;
                    firstLoading = true;
                    StartupLoader.onLoading();
                }
            }

            @Override
            public void onStarting(Plugin plugin) {
                // 加载监听器
                TListenerHandler.setupListener(plugin);
                // 加载插件类
                TabooLibLoader.postLoadClass(plugin, TabooLibLoader.getPluginClassSafely(plugin));
                // 注册插件命令
                TCommandHandler.registerCommand(plugin);
                // 首次运行
                if (!firstStarting && !(plugin instanceof InternalPlugin)) {
                    firstStarting = true;
                    StartupLoader.onStarting();
                }
            }

            @Override
            public void onActivated(Plugin plugin) {
                // 注册监听器
                TListenerHandler.registerListener(plugin);
                // 加载插件类
                TabooLibLoader.activeLoadClass(plugin, TabooLibLoader.getPluginClassSafely(plugin));
                // 注册调度器
                TScheduleLoader.run(plugin);
            }

            @Override
            public void onStopping(Plugin plugin) {
                // 保存数据
                Local.saveFiles(plugin.getName());
                Local.clearFiles(plugin.getName());
                // 释放文检读取
                Optional.ofNullable(TConfig.getFiles().remove(plugin.getName())).ifPresent(files -> files.forEach(file -> TConfigWatcher.getInst().removeListener(file)));
                // 非关服
                // 关服情况下不会主动卸载这些功能，因为进程即将关闭，等待其他插件最后处理
                if (!(plugin instanceof InternalPlugin)) {
                    // 注销插件类
                    TabooLibLoader.unloadClass(plugin, TabooLibLoader.getPluginClassSafely(plugin));
                    // 注销监听器
                    TListenerHandler.cancelListener(plugin);
                    // 注销数据库连接
                    DBSource.getDataSource().keySet().stream().filter(dbSourceData -> dbSourceData.getPlugin().equals(plugin)).forEach(DBSource::closeDataSource);
                    // 注销调度器
                    Bukkit.getScheduler().cancelTasks(plugin);
                    // 卸载语言文件
                    TLocaleLoader.unload(plugin);
                }
            }
        });
    }

    public void onLoading(Plugin plugin) {
    }

    public void postLoading(Plugin plugin) {
    }

    public void onStarting(Plugin plugin) {
    }

    public void postStarting(Plugin plugin) {
    }

    public void onActivated(Plugin plugin) {
    }

    public void onStopping(Plugin plugin) {
    }

    public void postStopping(Plugin plugin) {
    }

    public static void addPlugin(Plugin plugin) {
        plugins.add(plugin.getName());
    }

    public static void load(Plugin plugin) {
        registerLoader.forEach(loader -> loader.onLoading(plugin));
    }

    public static void postLoad(Plugin plugin) {
        registerLoader.forEach(loader -> loader.postLoading(plugin));
    }

    public static void start(Plugin plugin) {
        registerLoader.forEach(loader -> loader.onStarting(plugin));
    }

    public static void postStart(Plugin plugin) {
        registerLoader.forEach(loader -> loader.postStarting(plugin));
    }

    public static void active(Plugin plugin) {
        registerLoader.forEach(loader -> loader.onActivated(plugin));
    }

    public static void stop(Plugin plugin) {
        registerLoader.forEach(loader -> loader.onStopping(plugin));
    }

    public static void postStop(Plugin plugin) {
        registerLoader.forEach(loader -> loader.postStopping(plugin));
    }

    public static boolean isPlugin(Plugin plugin) {
        return plugins.contains(plugin.getName());
    }

    public static void redefine(Plugin origin, Object instance) {
        redefine.put(origin.getName(), instance);
    }

    public static Object get(Plugin plugin) {
        return redefine.getOrDefault(plugin.getName(), plugin);
    }

    public static Plugin getFirstLoaded() {
        return firstLoaded;
    }
}
