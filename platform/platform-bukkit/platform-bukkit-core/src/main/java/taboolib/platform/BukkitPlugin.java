package taboolib.platform;

import org.bukkit.Bukkit;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import taboolib.common.LifeCycle;
import taboolib.common.TabooLib;
import taboolib.common.boot.Environments;
import taboolib.common.io.ClassInstanceKt;
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
@PlatformSide(Platform.BUKKIT)
public class BukkitPlugin extends JavaPlugin {

    private static Plugin instanceDelegate;
    private static BukkitPlugin instance;

    static {
        TabooLib.booster().proceed(LifeCycle.CONST, Platform.BUKKIT);
        setupPluginInstance();
    }

    public BukkitPlugin() {
        instance = this;
        TabooLib.booster().proceed(LifeCycle.INIT);
    }

    @Override
    public void onLoad() {
        TabooLib.booster().proceed(LifeCycle.LOAD);
        setupPluginInstance();
        if (isRunning() && instanceDelegate != null) {
            instanceDelegate.onLoad();
        }
    }

    @Override
    public void onEnable() {
        TabooLib.booster().proceed(LifeCycle.ENABLE);
        if (isRunning() && instanceDelegate != null) {
            instanceDelegate.onEnable();
        }
        Bukkit.getScheduler().runTask(this, () -> {
            TabooLib.booster().proceed(LifeCycle.ACTIVE);
            if (isRunning() && instanceDelegate != null) {
                instanceDelegate.onActive();
            }
        });
    }

    @Override
    public void onDisable() {
        TabooLib.booster().proceed(LifeCycle.DISABLE);
        if (isRunning() && instanceDelegate != null) {
            instanceDelegate.onDisable();
        }
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(@NotNull String worldName, String id) {
        if (instanceDelegate instanceof BukkitWorldGenerator) {
            return ((BukkitWorldGenerator) instanceDelegate).getDefaultWorldGenerator(worldName, id);
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
        return instanceDelegate;
    }

    static boolean isRunning() {
        return !TabooLib.monitor().isShutdown();
    }

    static void setupPluginInstance() {
        if (Environments.isKotlin() && instanceDelegate == null) {
            instanceDelegate = ClassInstanceKt.findInstanceFromPlatform(Plugin.class);
        }
    }
}
