package taboolib.platform;

import org.bukkit.Bukkit;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import taboolib.common.LifeCycle;
import taboolib.common.TabooLib;
import taboolib.common.io.ClassInstanceKt;
import taboolib.common.platform.Platform;
import taboolib.common.platform.PlatformSide;
import taboolib.common.platform.Plugin;
import taboolib.common.platform.function.ExecutorKt;

import java.io.File;

/**
 * TabooLib
 * taboolib.platform.BukkitPlugin
 *
 * @author sky
 * @since 2021/6/26 8:22 下午
 */
@SuppressWarnings({"Convert2Lambda"})
@PlatformSide(Platform.BUKKIT)
public class BukkitPlugin extends JavaPlugin {

    @Nullable
    private static Plugin pluginInstance;
    private static BukkitPlugin instance;

    static {
        TabooLib.lifeCycle(LifeCycle.CONST, Platform.BUKKIT);
        if (TabooLib.isKotlinEnvironment()) {
            pluginInstance = ClassInstanceKt.findImplementation(Plugin.class);
        }
    }

    public BukkitPlugin() {
        instance = this;
        TabooLib.lifeCycle(LifeCycle.INIT);
    }

    @Override
    public void onLoad() {
        TabooLib.lifeCycle(LifeCycle.LOAD);
        if (pluginInstance == null) {
            pluginInstance = ClassInstanceKt.findImplementation(Plugin.class);
        }
        if (pluginInstance != null && !TabooLib.isStopped()) {
            pluginInstance.onLoad();
        }
    }

    @Override
    public void onEnable() {
        TabooLib.lifeCycle(LifeCycle.ENABLE);
        if (!TabooLib.isStopped()) {
            if (pluginInstance != null) {
                pluginInstance.onEnable();
            }
            try {
                ExecutorKt.startExecutor();
            } catch (NoClassDefFoundError ignored) {
            }
        }
        if (!TabooLib.isStopped()) {
            Bukkit.getScheduler().runTask(this, new Runnable() {
                @Override
                public void run() {
                    TabooLib.lifeCycle(LifeCycle.ACTIVE);
                    if (pluginInstance != null) {
                        pluginInstance.onActive();
                    }
                }
            });
        }
    }

    @Override
    public void onDisable() {
        TabooLib.lifeCycle(LifeCycle.DISABLE);
        if (pluginInstance != null && !TabooLib.isStopped()) {
            pluginInstance.onDisable();
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
}
