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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author 坏黑
 * @Since 2019-07-05 15:30
 */
public class TabooLibLoader {

    static Map<String, List<Class>> pluginClasses = Maps.newHashMap();
    static List<Loader> loaders = Lists.newArrayList();
    static List<Runnable> tasks = Lists.newArrayList();
    static boolean started;

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

    public static Optional<List<Class>> getPluginClasses(Plugin plugin) {
        return Optional.ofNullable(pluginClasses.get(plugin.getName()));
    }

    public static List<Class> getPluginClassSafely(Plugin plugin) {
        List<Class> classes = pluginClasses.get(plugin.getName());
        return classes == null ? new ArrayList<>() : new ArrayList<>(classes);
    }

    public static Map<String, List<Class>> getPluginClasses() {
        return pluginClasses;
    }

    public static List<Loader> getLoaders() {
        return loaders;
    }

    public static List<Runnable> getTasks() {
        return tasks;
    }

    public static boolean isStarted() {
        return started;
    }

    public static void runTask(Runnable runnable) {
        if (started) {
            runnable.run();
        } else {
            tasks.add(runnable);
        }
    }

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
        } catch (Exception ignored) {
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

    static boolean isLoader(Class pluginClass) {
        return !Loader.class.equals(pluginClass) && Loader.class.isAssignableFrom(pluginClass);
    }

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

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface IgnoreClasses {

        String[] value();

    }
}
