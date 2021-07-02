package taboolib.platform;

import org.bukkit.Bukkit;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import taboolib.common.LifeCycle;
import taboolib.common.TabooLibCommon;
import taboolib.common.io.IOKt;
import taboolib.common.platform.FunctionKt;
import taboolib.common.platform.Platform;
import taboolib.common.platform.PlatformSide;
import taboolib.common.platform.Plugin;

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
    private static final Plugin pluginInstance;
    private static BukkitPlugin instance;

    static {
        TabooLibCommon.lifeCycle(LifeCycle.CONST);
        pluginInstance = IOKt.findInstance(Plugin.class);
    }

    public BukkitPlugin() {
        instance = this;
        TabooLibCommon.lifeCycle(LifeCycle.INIT);
    }

    @Override
    public void onLoad() {
        TabooLibCommon.lifeCycle(LifeCycle.LOAD);
        if (pluginInstance != null) {
            pluginInstance.onLoad();
        }
    }

    @Override
    public void onEnable() {
        TabooLibCommon.lifeCycle(LifeCycle.ENABLE);
        if (pluginInstance != null) {
            pluginInstance.onEnable();
            Bukkit.getScheduler().runTask(this, new Runnable() {
                @Override
                public void run() {
                    TabooLibCommon.lifeCycle(LifeCycle.ACTIVE);
                    pluginInstance.onActive();
                }
            });
        }
        FunctionKt.startExecutor();
    }

    @Override
    public void onDisable() {
        TabooLibCommon.lifeCycle(LifeCycle.DISABLE);
        if (pluginInstance != null) {
            pluginInstance.onDisable();
        }
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        if (pluginInstance instanceof BukkitWorldGenerator) {
            return ((BukkitWorldGenerator) pluginInstance).getDefaultWorldGenerator(worldName, id);
        }
        return null;
    }

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
