package taboolib.platform;

import net.md_5.bungee.BungeeCord;
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
import java.util.concurrent.TimeUnit;

/**
 * TabooLib
 * taboolib.platform.BungeePlugin
 *
 * @author sky
 * @since 2021/6/26 8:22 下午
 */
@PlatformSide(Platform.BUNGEE)
public class BungeePlugin extends net.md_5.bungee.api.plugin.Plugin {

    private static Plugin instanceDelegate;
    private static BungeePlugin instance;

    static {
        TabooLib.booster().proceed(LifeCycle.CONST, Platform.BUNGEE);
        setupPluginInstance();
    }

    public BungeePlugin() {
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
        BungeeCord.getInstance().getScheduler().schedule(this, () -> {
            TabooLib.booster().proceed(LifeCycle.ACTIVE);
            if (isRunning() && instanceDelegate != null) {
                instanceDelegate.onActive();
            }
        }, 0, TimeUnit.SECONDS);
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
    public static BungeePlugin getInstance() {
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
