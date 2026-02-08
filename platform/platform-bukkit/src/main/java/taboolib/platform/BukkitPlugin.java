package taboolib.platform;

import org.bukkit.Bukkit;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import taboolib.common.LifeCycle;
import taboolib.common.PrimitiveIO;
import taboolib.common.TabooLib;
import taboolib.common.classloader.IsolatedClassLoader;
import taboolib.common.platform.Platform;
import taboolib.common.platform.PlatformSide;
import taboolib.common.platform.Plugin;

import java.io.File;

import static taboolib.common.PrimitiveIO.t;

/**
 * TabooLib
 * taboolib.platform.BukkitPlugin
 *
 * @author sky
 * @since 2021/6/26 8:22 下午
 */
@SuppressWarnings({"DuplicatedCode", "CallToPrintStackTrace"})
@PlatformSide(Platform.BUKKIT)
public class BukkitPlugin extends JavaPlugin {

    @Nullable
    private static Plugin pluginInstance;
    private static BukkitPlugin instance;

    static {
        PrimitiveIO.debug("Bukkit 插件初始化完成，用时 {0} 毫秒。", TabooLib.execution(() -> {
            try {
                // 初始化 IsolatedClassLoader
                IsolatedClassLoader.init(BukkitPlugin.class);
                // 排除两个接口
                IsolatedClassLoader.INSTANCE.addExcludedClass("taboolib.platform.BukkitWorldGenerator");
                IsolatedClassLoader.INSTANCE.addExcludedClass("taboolib.platform.BukkitBiomeProvider");
            } catch (Throwable ex) {
                TabooLib.setStopped(true);
                PrimitiveIO.error(
                        t(
                                "无法初始化原始加载器，插件 \"{0}\" 将被禁用！",
                                "Failed to initialize primitive loader, the plugin \"{0}\" will be disabled!"
                        ),
                        PrimitiveIO.getRunningFileName()
                );
                throw ex;
            }
            // 生命周期任务
            TabooLib.lifeCycle(LifeCycle.CONST);
            // 检索 TabooLib Plugin 实现
            pluginInstance = Plugin.getInstance();
        }));
    }

    public BukkitPlugin() {
        instance = this;
        // 修改访问提示（似乎有用）
        IllegalAccess.inject();
        // 生命周期任务
        TabooLib.lifeCycle(LifeCycle.INIT);
    }

    @Override
    public void onLoad() {
        // 生命周期任务
        TabooLib.lifeCycle(LifeCycle.LOAD);
        // 调用 Plugin 实现的 onLoad() 方法
        if (pluginInstance != null && !TabooLib.isStopped()) {
            pluginInstance.onLoad();
        }
    }

    @Override
    public void onEnable() {
        // 生命周期任务
        TabooLib.lifeCycle(LifeCycle.ENABLE);
        // 判断插件是否关闭
        if (!TabooLib.isStopped()) {
            // 调用 Plugin 实现的 onEnable() 方法
            if (pluginInstance != null) {
                pluginInstance.onEnable();
            }
        }
        // 再次判断插件是否关闭
        // 因为插件可能在 onEnable() 下关闭
        if (!TabooLib.isStopped()) {
            // 创建调度器，执行 onActive() 方法
            if (Folia.isFolia) {
                FoliaExecutor.ASYNC_SCHEDULER.runNow(this, task -> invokeActive());
            } else {
                Bukkit.getScheduler().runTask(this, this::invokeActive);
            }
        }
    }

    @Override
    public void onDisable() {
        // 在插件未关闭的前提下，执行 onDisable() 方法
        if (pluginInstance != null && !TabooLib.isStopped()) {
            pluginInstance.onDisable();
        }
        // 生命周期任务
        TabooLib.lifeCycle(LifeCycle.DISABLE);
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(@NotNull String worldName, @Nullable String id) {
        if (pluginInstance instanceof BukkitWorldGenerator) {
            return ((BukkitWorldGenerator) pluginInstance).getDefaultWorldGenerator(worldName, id);
        }
        return null;
    }

    @Nullable
    @Override
    public BiomeProvider getDefaultBiomeProvider(@NotNull String worldName, @Nullable String id) {
        if (pluginInstance instanceof BukkitBiomeProvider) {
            return ((BukkitBiomeProvider) pluginInstance).getDefaultBiomeProvider(worldName, id);
        }
        return null;
    }

    @NotNull
    @Override
    public File getFile() {
        return super.getFile();
    }

    @Nullable
    public static Plugin getPluginInstance() {
        return pluginInstance;
    }

    @NotNull
    public static BukkitPlugin getInstance() {
        return instance;
    }

    /**
     * 运行 onActive() 方法
     */
    private void invokeActive() {
        // 生命周期任务
        TabooLib.lifeCycle(LifeCycle.ACTIVE);
        // 调用 Plugin 实现的 onActive() 方法
        if (pluginInstance != null) {
            pluginInstance.onActive();
        }
    }
}
