package taboolib.platform;

import net.md_5.bungee.BungeeCord;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import taboolib.common.LifeCycle;
import taboolib.common.TabooLib;
import taboolib.common.io.ClassInstanceKt;
import taboolib.common.platform.Platform;
import taboolib.common.platform.PlatformSide;
import taboolib.common.platform.Plugin;
import taboolib.common.platform.function.ExecutorKt;

import java.util.concurrent.TimeUnit;

/**
 * TabooLib
 * taboolib.platform.BungeePlugin
 *
 * @author sky
 * @since 2021/6/26 8:22 下午
 */
@SuppressWarnings({"Convert2Lambda"})
@PlatformSide(Platform.BUNGEE)
public class BungeePlugin extends net.md_5.bungee.api.plugin.Plugin {

    @Nullable
    private static Plugin pluginInstance;
    private static BungeePlugin instance;

    static {
        TabooLib.booster().proceed(LifeCycle.CONST, Platform.BUNGEE);
        if (TabooLib.isKotlinEnvironment()) {
            pluginInstance = ClassInstanceKt.findInstanceFromPlatform(Plugin.class);
        }
    }

    public BungeePlugin() {
        instance = this;
        TabooLib.booster().proceed(LifeCycle.INIT);
    }

    @Override
    public void onLoad() {
        TabooLib.booster().proceed(LifeCycle.LOAD);
        if (pluginInstance == null) {
            pluginInstance = ClassInstanceKt.findInstanceFromPlatform(Plugin.class);
        }
        if (pluginInstance != null && !TabooLib.isStopped()) {
            pluginInstance.onLoad();
        }
    }

    @Override
    public void onEnable() {
        TabooLib.booster().proceed(LifeCycle.ENABLE);
        if (!TabooLib.isStopped()) {
            if (pluginInstance != null) {
                pluginInstance.onEnable();
            }
            try {
                ExecutorKt.startNow();
            } catch (NoClassDefFoundError ignored) {
            }
        }
        if (!TabooLib.isStopped()) {
            BungeeCord.getInstance().getScheduler().schedule(this, new Runnable() {
                @Override
                public void run() {
                    TabooLib.booster().proceed(LifeCycle.ACTIVE);
                    if (pluginInstance != null) {
                        pluginInstance.onActive();
                    }
                }
            }, 0, TimeUnit.SECONDS);
        }
    }

    @Override
    public void onDisable() {
        TabooLib.booster().proceed(LifeCycle.DISABLE);
        if (pluginInstance != null && !TabooLib.isStopped()) {
            pluginInstance.onDisable();
        }
    }

    @NotNull
    public static BungeePlugin getInstance() {
        return instance;
    }

    @Nullable
    public static Plugin getPluginInstance() {
        return pluginInstance;
    }
}
