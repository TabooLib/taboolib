package taboolib.platform;

import cn.nukkit.Server;
import cn.nukkit.plugin.PluginBase;
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
@SuppressWarnings({"FieldCanBeLocal"})
@PlatformSide(Platform.NUKKIT)
public class NukkitPlugin extends PluginBase {

    private static Plugin instanceDelegate;
    private static NukkitPlugin instance;

    static {
        TabooLib.booster().proceed(LifeCycle.CONST, Platform.NUKKIT);
        setupPluginInstance();
    }

    public NukkitPlugin() {
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
        if (isRunning()) {
            Server.getInstance().getScheduler().scheduleTask(this, () -> {
                TabooLib.booster().proceed(LifeCycle.ACTIVE);
                if (isRunning() && instanceDelegate != null) {
                    instanceDelegate.onActive();
                }
            });
        }
    }

    @Override
    public void onDisable() {
        TabooLib.booster().proceed(LifeCycle.DISABLE);
        if (isRunning() && instanceDelegate != null) {
            instanceDelegate.onDisable();
        }
    }

    @NotNull
    @Override
    public File getFile() {
        return super.getFile();
    }

    @NotNull
    public static NukkitPlugin getInstance() {
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
