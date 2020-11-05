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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * TabooLib 插件加载器，只允许 TabooLib 内部使用。
 *
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
                TDependencyInjector.inject(plugin, PluginLoader.get(plugin).getClass());
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

    /**
     * 当插件载入（onLoad）时
     *
     * @param plugin 插件实例
     */
    public void onLoading(Plugin plugin) {
    }

    /**
     * 当插件载入完成时
     *
     * @param plugin 插件实例
     */
    public void postLoading(Plugin plugin) {
    }

    /**
     * 当插件启动（onEnable）时
     *
     * @param plugin 插件实例
     */
    public void onStarting(Plugin plugin) {
    }

    /**
     * 当插件启动完成时
     *
     * @param plugin 插件实例
     */
    public void postStarting(Plugin plugin) {
    }

    /**
     * 当插件活跃（可执行 Bukkit 插件调度器）时
     *
     * @param plugin 插件实例
     */
    public void onActivated(Plugin plugin) {
    }

    /**
     * 当插件停止（onDisable）时
     *
     * @param plugin 插件实例
     */
    public void onStopping(Plugin plugin) {
    }

    /**
     * 当插件已停止时
     *
     * @param plugin 插件实例
     */
    public void postStopping(Plugin plugin) {
    }

    /**
     * 认可一个基于 TabooLib 的插件
     *
     * @param plugin 插件实例
     */
    public static void addPlugin(Plugin plugin) {
        plugins.add(plugin.getName());
    }

    /**
     * 调用所有插件加载器为该插件执行 onLoading 方法
     *
     * @param plugin 插件实例
     */
    public static void load(Plugin plugin) {
        registerLoader.forEach(loader -> loader.onLoading(plugin));
    }

    /**
     * 调用所有插件加载器为该插件执行 postLoading 方法
     *
     * @param plugin 插件实例
     */
    public static void postLoad(Plugin plugin) {
        registerLoader.forEach(loader -> loader.postLoading(plugin));
    }

    /**
     * 调用所有插件加载器为该插件执行 onStarting 方法
     *
     * @param plugin 插件实例
     */
    public static void start(Plugin plugin) {
        registerLoader.forEach(loader -> loader.onStarting(plugin));
    }

    /**
     * 调用所有插件加载器为该插件执行 postStarting 方法
     *
     * @param plugin 插件实例
     */
    public static void postStart(Plugin plugin) {
        registerLoader.forEach(loader -> loader.postStarting(plugin));
    }

    /**
     * 调用所有插件加载器为该插件执行 onActivated 方法
     *
     * @param plugin 插件实例
     */
    public static void active(Plugin plugin) {
        registerLoader.forEach(loader -> loader.onActivated(plugin));
    }

    /**
     * 调用所有插件加载器为该插件执行 onStopping 方法
     *
     * @param plugin 插件实例
     */
    public static void stop(Plugin plugin) {
        registerLoader.forEach(loader -> loader.onStopping(plugin));
    }

    /**
     * 调用所有插件加载器为该插件执行 postStopping 方法
     *
     * @param plugin 插件实例
     */
    public static void postStop(Plugin plugin) {
        registerLoader.forEach(loader -> loader.postStopping(plugin));
    }

    /**
     * 检测该插件是否被 TabooLib 认可
     *
     * @param plugin 插件实例
     */
    public static boolean isPlugin(Plugin plugin) {
        return plugins.contains(plugin.getName());
    }

    /**
     * 重定义一个插件的在 TabooLib 中的主类地址
     *
     * @param origin   插件实例
     * @param instance 重定义主类实例
     */
    public static void redefine(Plugin origin, @NotNull Object instance) {
        redefine.put(origin.getName(), instance);
    }

    /**
     * 获取一个插件在 TabooLib 中的主类
     *
     * @param plugin 插件实例
     * @return 主类实例（可能不继承自 JavaPlugin）
     */
    @NotNull
    public static Object get(Plugin plugin) {
        return redefine.getOrDefault(plugin.getName(), plugin);
    }

    /**
     * 获取首个被 TabooLib 的插件
     *
     * @return 插件实例
     */
    @Nullable
    public static Plugin getFirstLoaded() {
        return firstLoaded;
    }
}
