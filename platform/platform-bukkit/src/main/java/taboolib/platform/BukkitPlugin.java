package taboolib.platform;

import org.bukkit.Bukkit;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tabooproject.reflex.Reflex;
import taboolib.common.LifeCycle;
import taboolib.common.TabooLibCommon;
import taboolib.common.classloader.IsolatedClassLoader;
import taboolib.common.io.Project1Kt;
import taboolib.common.platform.Platform;
import taboolib.common.platform.PlatformSide;
import taboolib.common.platform.Plugin;
import taboolib.common.platform.function.ExecutorKt;
import taboolib.module.lang.Language;
import taboolib.platform.lang.TypeBossBar;

import java.io.File;
import java.net.URL;
import java.util.Set;

/**
 * TabooLib
 * taboolib.platform.BukkitPlugin
 *
 * @author sky
 * @since 2021/6/26 8:22 下午
 */
@SuppressWarnings({"Convert2Lambda", "DuplicatedCode", "CallToPrintStackTrace"})
@PlatformSide(Platform.BUKKIT)
public class BukkitPlugin extends JavaPlugin {

    @Nullable
    private static Plugin pluginInstance;
    private static BukkitPlugin instance;
    private static Class<?> delegateClass;
    private static Object delegateObject;
    @Nullable
    private static IsolatedClassLoader isolatedClassLoader;

    static {
        if (IsolatedClassLoader.isEnabled()) {
            try {
                IsolatedClassLoader loader = new IsolatedClassLoader(
                        new URL[]{BukkitPlugin.class.getProtectionDomain().getCodeSource().getLocation()},
                        BukkitPlugin.class.getClassLoader()
                );
                isolatedClassLoader = loader;
                delegateClass = Class.forName("taboolib.platform.BukkitPluginDelegate", true, loader);
                delegateObject = delegateClass.getConstructor().newInstance();
                delegateClass.getMethod("onConst").invoke(delegateObject);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            TabooLibCommon.lifeCycle(LifeCycle.CONST, Platform.BUKKIT);
            // 搜索 Plugin 实现
            if (TabooLibCommon.isKotlinEnvironment()) {
                pluginInstance = Project1Kt.findImplementation(Plugin.class);
            }
            // 注册语言文件
            try {
                Language.INSTANCE.getLanguageType().put("boss", TypeBossBar.class);
            } catch (Throwable ignored) {
            }
        }
    }

    public BukkitPlugin() {
        instance = this;
        if (IsolatedClassLoader.isEnabled()) {
            try {
                delegateClass.getMethod("onInit").invoke(delegateObject);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            // 修改访问提示（似乎有用）
            injectAccess();
            // 生命周期
            TabooLibCommon.lifeCycle(LifeCycle.INIT);
        }
    }

    @Override
    public void onLoad() {
        if (IsolatedClassLoader.isEnabled()) {
            try {
                delegateClass.getMethod("onLoad").invoke(delegateObject);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            TabooLibCommon.lifeCycle(LifeCycle.LOAD);
            // 再次尝试搜索 Plugin 实现
            if (pluginInstance == null) {
                pluginInstance = Project1Kt.findImplementation(Plugin.class);
            }
            // 调用 Plugin 实现的 onLoad() 方法
            if (pluginInstance != null && !TabooLibCommon.isStopped()) {
                pluginInstance.onLoad();
            }
        }
    }

    @Override
    public void onEnable() {
        if (IsolatedClassLoader.isEnabled()) {
            try {
                delegateClass.getMethod("onEnable").invoke(delegateObject);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            TabooLibCommon.lifeCycle(LifeCycle.ENABLE);
            // 判断插件是否关闭
            if (!TabooLibCommon.isStopped()) {
                // 调用 onEnable() 方法
                if (pluginInstance != null) {
                    pluginInstance.onEnable();
                }
                // 启动调度器
                try {
                    ExecutorKt.startExecutor();
                } catch (NoClassDefFoundError ignored) {
                }
            }
            // 再次判断插件是否关闭
            // 因为插件可能在 onEnable() 下关闭
            if (!TabooLibCommon.isStopped()) {
                // 创建调度器，执行 onActive() 方法
                Bukkit.getScheduler().runTask(this, new Runnable() {
                    @Override
                    public void run() {
                        TabooLibCommon.lifeCycle(LifeCycle.ACTIVE);
                        if (pluginInstance != null) {
                            pluginInstance.onActive();
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onDisable() {
        if (IsolatedClassLoader.isEnabled()) {
            try {
                delegateClass.getMethod("onDisable").invoke(delegateObject);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            // 在插件未关闭的前提下，执行 onDisable() 方法
            if (pluginInstance != null && !TabooLibCommon.isStopped()) {
                pluginInstance.onDisable();
            }
            TabooLibCommon.lifeCycle(LifeCycle.DISABLE);
        }
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(@NotNull String worldName, String id) {
        try {
            if (pluginInstance instanceof BukkitWorldGenerator) {
                return ((BukkitWorldGenerator) pluginInstance).getDefaultWorldGenerator(worldName, id);
            }
        } catch (NoClassDefFoundError ignored) {
        }
        return null;
    }

    @NotNull
    @Override
    public File getFile() {
        return super.getFile();
    }

    @NotNull
    public static BukkitPlugin getInstance() {
        return instance;
    }

    @Nullable
    public static Plugin getPluginInstance() {
        return pluginInstance;
    }
    
    @Nullable
    public static IsolatedClassLoader getIsolatedClassLoader() {
        return isolatedClassLoader;
    }

    /**
     * 移除 Spigot 的访问警告：
     * Loaded class {0} from {1} which is not a depend, softdepend or loadbefore of this plugin
     */
    @SuppressWarnings("DataFlowIssue")
    static void injectAccess() {
        try {
            PluginDescriptionFile description = Reflex.Companion.getProperty(BukkitPlugin.class.getClassLoader(), "description", false, true, false);
            Set<String> accessSelf = Reflex.Companion.getProperty(BukkitPlugin.class.getClassLoader(), "seenIllegalAccess", false, true, false);
            for (org.bukkit.plugin.Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                if (plugin.getClass().getName().endsWith("platform.BukkitPlugin")) {
                    Set<String> accessOther = Reflex.Companion.getProperty(plugin.getClass().getClassLoader(), "seenIllegalAccess", false, true, false);
                    accessOther.add(description.getName());
                    accessSelf.add(plugin.getName());
                }
            }
        } catch (Throwable ignored) {
        }
    }
}
