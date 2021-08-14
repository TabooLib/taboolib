package taboolib.platform;

import net.md_5.bungee.BungeeCord;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import taboolib.common.LifeCycle;
import taboolib.common.TabooLibCommon;
import taboolib.common.io.Project1Kt;
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
        TabooLibCommon.lifeCycle(LifeCycle.CONST, Platform.BUNGEE);
        if (TabooLibCommon.isKotlinEnvironment()) {
            pluginInstance = Project1Kt.findImplementation(Plugin.class);
        }
    }

    public BungeePlugin() {
        instance = this;
        TabooLibCommon.lifeCycle(LifeCycle.INIT);
    }

    @Override
    public void onLoad() {
        TabooLibCommon.lifeCycle(LifeCycle.LOAD);
        if (pluginInstance == null) {
            pluginInstance = Project1Kt.findImplementation(Plugin.class);
        }
        if (pluginInstance != null && !TabooLibCommon.isStopped()) {
            pluginInstance.onLoad();
        }
    }

    @Override
    public void onEnable() {
        TabooLibCommon.lifeCycle(LifeCycle.ENABLE);
        if (pluginInstance != null && !TabooLibCommon.isStopped()) {
            pluginInstance.onEnable();
            BungeeCord.getInstance().getScheduler().schedule(this, new Runnable() {
                @Override
                public void run() {
                    TabooLibCommon.lifeCycle(LifeCycle.ACTIVE);
                    pluginInstance.onActive();
                }
            }, 0, TimeUnit.SECONDS);
        }
        try {
            ExecutorKt.startExecutor();
        } catch (NoClassDefFoundError ignored) {
        }
    }

    @Override
    public void onDisable() {
        TabooLibCommon.lifeCycle(LifeCycle.DISABLE);
        if (pluginInstance != null && !TabooLibCommon.isStopped()) {
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
