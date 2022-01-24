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
        TabooLib.lifeCycle(LifeCycle.CONST, Platform.BUNGEE);
        if (TabooLib.isKotlinEnvironment()) {
            pluginInstance = ClassInstanceKt.findImplementation(Plugin.class);
        }
    }

    public BungeePlugin() {
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
            BungeeCord.getInstance().getScheduler().schedule(this, new Runnable() {
                @Override
                public void run() {
                    TabooLib.lifeCycle(LifeCycle.ACTIVE);
                    if (pluginInstance != null) {
                        pluginInstance.onActive();
                    }
                }
            }, 0, TimeUnit.SECONDS);
        }
    }

    @Override
    public void onDisable() {
        TabooLib.lifeCycle(LifeCycle.DISABLE);
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
