package io.izzel.taboolib;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.izzel.taboolib.client.TabooLibClient;
import io.izzel.taboolib.client.TabooLibServer;
import io.izzel.taboolib.metrics.BStats;
import io.izzel.taboolib.module.dependency.TDependencyInjector;
import io.izzel.taboolib.module.inject.TSchedule;
import io.izzel.taboolib.util.Files;
import io.izzel.taboolib.util.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TabooLib 插件加载类
 *
 * @Author 坏黑
 * @Since 2019-07-05 15:30
 */
@SuppressWarnings("rawtypes")
public class TabooLibLoader {

    static Map<String, List<Class>> pluginClasses = Maps.newConcurrentMap();
    static List<Loader> loaders = Lists.newArrayList();
    static List<Runnable> tasks = Lists.newArrayList();
    static boolean started;

    /**
     * 通过以下顺序初始化
     * 1. 加载依赖
     * 2. 加载插件统计
     * 3. 读取插件类
     * 4. 读取加载器
     * 5. 载入 TabooLib 伪装插件
     * 6. 启动 TabooLib 伪装插件
     */
    static void init() {
        // 加载依赖
        TDependencyInjector.inject(TabooLib.getPlugin(), TabooLib.class);
        // 插件统计
        BStats bStats = new BStats(TabooLib.getPlugin());
        bStats.addCustomChart(new BStats.SingleLineChart("plugins_using_taboolib", () -> Math.toIntExact(Arrays.stream(Bukkit.getPluginManager().getPlugins()).filter(TabooLibAPI::isDependTabooLib).count())));
        // 读取插件类
        setupClasses(TabooLib.getPlugin());
        // 读取加载器
        pluginClasses.get("TabooLib").stream().filter(TabooLibLoader::isLoader).forEach(pluginClass -> {
            try {
                loaders.add((Loader) Reflection.instantiateObject(pluginClass));
            } catch (Exception e) {
                e.printStackTrace();
            }
            loaders.sort(Comparator.comparingInt(Loader::priority));
        });
        // 加载插件
        PluginLoader.load(TabooLib.getPlugin());
        PluginLoader.start(TabooLib.getPlugin());
    }

    /**
     * 获取插件的所有类，需要该插件基于 TabooLib
     *
     * @param plugin 插件实例
     */
    @NotNull
    public static Optional<List<Class>> getPluginClasses(Plugin plugin) {
        return Optional.ofNullable(pluginClasses.get(plugin.getName()));
    }

    /**
     * 获取插件的所有类，需要该插件基于 TabooLib
     *
     * @param plugin 插件实例
     */
    @NotNull
    public static List<Class> getPluginClassSafely(Plugin plugin) {
        List<Class> classes = pluginClasses.get(plugin.getName());
        return classes == null ? new ArrayList<>() : new ArrayList<>(classes);
    }

    /**
     * 获取已缓存的所有插件类
     */
    @NotNull
    public static Map<String, List<Class>> getPluginClasses() {
        return pluginClasses;
    }

    /**
     * 获取所有加载器 {@link Loader}
     */
    @NotNull
    public static List<Loader> getLoaders() {
        return loaders;
    }

    /**
     * 获取所有启动计划
     */
    @NotNull
    public static List<Runnable> getTasks() {
        return tasks;
    }

    /**
     * 检查 TabooLib 是否完全启动
     */
    public static boolean isStarted() {
        return started;
    }

    /**
     * 运行启动计划，用于防止在插件未启动时运行 BukkitRunnable 报错
     * 由 TabooLib 代理执行
     */
    public static void runTask(Runnable runnable) {
        if (started) {
            runnable.run();
        } else {
            tasks.add(runnable);
        }
    }

    /**
     * 通过以下顺序启动 TabooLib
     * 1. 执行 active 计划
     * 2. 启动本地通讯服务器
     * 3. 启动本地通讯客户端
     * 4. 执行启动计划
     */
    @TSchedule
    static void start() {
        PluginLoader.active(TabooLib.getPlugin());
        // 通讯网络服务器
        if (TabooLib.getConfig().getBoolean("SERVER")) {
            TabooLibServer.main(new String[0]);
        }
        // 通讯网络客户端
        TabooLibClient.init();
        // 执行动作
        for (Runnable runnable : tasks) {
            try {
                runnable.run();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        started = true;
    }

    /**
     * 缓存该插件的所有类，如果该插件名为 TabooLib 则只读取 io.izzel 包。
     * 通过添加 {@link IgnoreClasses} 注解忽略特定包，避免造成内存浪费。
     *
     * @param plugin 插件实例
     */
    static void setupClasses(Plugin plugin) {
        try {
            long time = System.currentTimeMillis();
            List<Class> classes;
            IgnoreClasses annotation = plugin.getClass().getAnnotation(IgnoreClasses.class);
            if (annotation != null) {
                classes = Files.getClasses(plugin, annotation.value());
            } else {
                classes = Files.getClasses(plugin);
            }
            if (plugin.getName().equals("TabooLib")) {
                classes = classes.stream().filter(c -> c.getName().startsWith("io.izzel")).collect(Collectors.toList());
            }
            TabooLibAPI.debug("Saved " + classes.size() + " classes (" + plugin.getName() + ") (" + (System.currentTimeMillis() - time) + "ms)");
            pluginClasses.put(plugin.getName(), classes);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    static void preLoadClass(Plugin plugin, List<Class> loadClass) {
        loaders.forEach(loader -> {
            for (Class pluginClass : loadClass) {
                try {
                    loader.preLoad(plugin, pluginClass);
                } catch (NoClassDefFoundError ignore) {
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
    }

    static void postLoadClass(Plugin plugin, List<Class> loadClass) {
        loaders.forEach(loader -> {
            for (Class pluginClass : loadClass) {
                try {
                    loader.postLoad(plugin, pluginClass);
                } catch (NoClassDefFoundError ignore) {
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
    }

    static void activeLoadClass(Plugin plugin, List<Class> loadClass) {
        loaders.forEach(loader -> {
            for (Class pluginClass : loadClass) {
                try {
                    loader.activeLoad(plugin, pluginClass);
                } catch (NoClassDefFoundError ignore) {
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
    }

    static void unloadClass(Plugin plugin, List<Class> loadClass) {
        loaders.forEach(loader -> {
            for (Class pluginClass : loadClass) {
                try {
                    loader.unload(plugin, pluginClass);
                } catch (NoClassDefFoundError ignore) {
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 判定该类是否为加载器
     *
     * @param pluginClass 插件类
     */
    static boolean isLoader(Class pluginClass) {
        return !Loader.class.equals(pluginClass) && Loader.class.isAssignableFrom(pluginClass);
    }

    /**
     * 加载器接口
     * 只允许 TabooLib 自身使用
     */
    public interface Loader {

        default void preLoad(Plugin plugin, Class<?> pluginClass) {
        }

        default void postLoad(Plugin plugin, Class<?> pluginClass) {
        }

        default void activeLoad(Plugin plugin, Class<?> pluginClass) {
        }

        default void unload(Plugin plugin, Class<?> pluginClass) {
        }

        default int priority() {
            return 0;
        }
    }

    /**
     * 在 TabooLib 缓存插件类时屏蔽特定包名
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface IgnoreClasses {

        /**
         * 包名（左模糊判断）
         */
        String[] value();

    }
}
