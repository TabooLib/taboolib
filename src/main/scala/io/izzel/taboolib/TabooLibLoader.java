package io.izzel.taboolib;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.izzel.taboolib.client.TabooLibClient;
import io.izzel.taboolib.client.TabooLibServer;
import io.izzel.taboolib.kotlin.Reflex;
import io.izzel.taboolib.metrics.BMetrics;
import io.izzel.taboolib.module.dependency.TDependencyInjector;
import io.izzel.taboolib.module.inject.TSchedule;
import io.izzel.taboolib.util.Files;
import io.izzel.taboolib.util.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TabooLib 插件加载类
 *
 * @author 坏黑
 * @since 2019-07-05 15:30
 */
@SuppressWarnings("rawtypes")
public class TabooLibLoader {

    static Map<String, List<Class<?>>> pluginClasses = Maps.newConcurrentMap();
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
        BMetrics metrics = new BMetrics(TabooLib.getPlugin(), 2187);
        metrics.addCustomChart(new BMetrics.SingleLineChart("plugins_using_taboolib", () -> {
            int count = 0;
            for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                if (TabooLibAPI.isDependTabooLib(plugin)) {
                    count++;
                }
            }
            return count;
        }));
        metrics.addCustomChart(new BMetrics.AdvancedPie("plugins", () -> {
            Map<String, Integer> map = new HashMap<>();
            for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                if (TabooLibAPI.isDependTabooLib(plugin)) {
                    map.put(plugin.getName(), map.getOrDefault(plugin.getName(), 0) + 1);
                }
                if (plugin.getDescription().getDepend().contains("TabooLib")) {
                    map.put(plugin.getName() + " (Legacy 4)", map.getOrDefault(plugin.getName(), 0) + 1);
                }
            }
            return map;
        }));
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
     * @return List
     */
    @NotNull
    public static Optional<List<Class<?>>> getPluginClasses(Plugin plugin) {
        return Optional.ofNullable(pluginClasses.get(plugin.getName()));
    }

    /**
     * 获取插件的所有类，需要该插件基于 TabooLib
     *
     * @param plugin 插件实例
     * @return List
     */
    @NotNull
    public static List<Class<?>> getPluginClassSafely(Plugin plugin) {
        List<Class<?>> classes = pluginClasses.get(plugin.getName());
        return classes == null ? new ArrayList<>() : new ArrayList<>(classes);
    }

    /**
     * @return 已缓存的所有插件类
     */
    @NotNull
    public static Map<String, List<Class<?>>> getPluginClasses() {
        return pluginClasses;
    }

    /**
     * @return 所有加载器 {@link Loader}
     */
    @NotNull
    public static List<Loader> getLoaders() {
        return loaders;
    }

    /**
     * @return 所有启动计划
     */
    @NotNull
    public static List<Runnable> getTasks() {
        return tasks;
    }

    /**
     * @return TabooLib 是否完全启动
     */
    public static boolean isStarted() {
        return started;
    }

    /**
     * 运行启动计划，用于防止在插件未启动时运行 BukkitRunnable 报错
     * 由 TabooLib 代理执行
     *
     * @param runnable 计划
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
            final List<Class<?>> classes = Lists.newArrayList();
            File file = plugin.getName().equals("TabooLib") ? TabooLib.getFile() : Reflex.Companion.from(JavaPlugin.class).instance(plugin).read("file");
            File fileClasses = Files.file(TabooLib.getPlugin().getDataFolder(), "cache/classes/" + plugin.getName() + ".txt");
            Files.read(fileClasses, r -> {
                String fileHash = Files.getFileHash(file, "SHA-1");
                List<String> lines = r.lines().collect(Collectors.toList());
                if (lines.size() > 3 && Objects.equals(fileHash, lines.get(1))) {
                    for (int i = 1; i < lines.size(); i++) {
                        try {
                            if (lines.get(i).length() > 0) {
                                classes.add(Class.forName(lines.get(i), false, plugin.getClass().getClassLoader()));
                            }
                        } catch (Throwable ignored) {
                        }
                    }
                } else {
                    classes.addAll(getClassesFromJar(plugin));
                    // 异步写入缓存
                    Bukkit.getScheduler().runTaskAsynchronously(TabooLib.getPlugin(), () -> {
                        Files.write(fileClasses, w -> {
                            w.write("--- SHA-1 ---");
                            w.newLine();
                            w.write(fileHash != null ? fileHash : "");
                            w.newLine();
                            w.write("--- SHA-1 ---");
                            w.newLine();
                            w.newLine();
                            for (Class c : classes) {
                                w.write(c.getName());
                                w.newLine();
                            }
                        });
                    });
                }
            });
            pluginClasses.put(plugin.getName(), classes);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    static List<Class<?>> getClassesFromJar(Plugin plugin) {
        List<Class<?>> classes = Lists.newArrayList();
        IgnoreClasses annotation = plugin.getClass().getAnnotation(IgnoreClasses.class);
        if (annotation != null) {
            classes.addAll(Files.getClasses(plugin, annotation.value()));
        } else {
            classes.addAll(Files.getClasses(plugin));
        }
        if (plugin.getName().equals("TabooLib")) {
            classes.removeIf(p -> !p.getName().startsWith("io.izzel"));
        }
        return classes;
    }

    static void preLoadClass(Plugin plugin, List<Class<?>> loadClass) {
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

    static void postLoadClass(Plugin plugin, List<Class<?>> loadClass) {
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

    static void activeLoadClass(Plugin plugin, List<Class<?>> loadClass) {
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

    static void unloadClass(Plugin plugin, List<Class<?>> loadClass) {
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
         * @return 包名（左模糊判断）
         */
        String[] value();

    }
}
