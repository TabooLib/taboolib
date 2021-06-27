package taboolib.platform;

import net.md_5.bungee.BungeeCord;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import taboolib.common.TabooLibCommon;
import taboolib.common.io.IOKt;
import taboolib.common.platform.FunctionKt;
import taboolib.common.platform.Platform;
import taboolib.common.platform.PlatformSide;
import taboolib.common.platform.Plugin;

import java.util.concurrent.TimeUnit;

/**
 * TabooLib
 * taboolib.platform.BungeePlugin
 *
 * @author sky
 * @since 2021/6/26 8:22 下午
 */
@SuppressWarnings({"Anonymous2MethodRef", "Convert2Lambda"})
@PlatformSide(Platform.BUNGEE)
public class BungeePlugin extends net.md_5.bungee.api.plugin.Plugin {

    @Nullable
    private static final Plugin pluginInstance;
    private static BungeePlugin instance;

    static {
        TabooLibCommon.init();
        pluginInstance = IOKt.findInstance(Plugin.class);
    }

    public BungeePlugin() {
        instance = this;
    }

    @Override
    public void onLoad() {
        if (pluginInstance != null) {
            pluginInstance.onLoad();
        }
    }

    @Override
    public void onEnable() {
        if (pluginInstance != null) {
            pluginInstance.onEnable();
            BungeeCord.getInstance().getScheduler().schedule(this, new Runnable() {
                @Override
                public void run() {
                    pluginInstance.onActive();
                }
            }, 0, TimeUnit.SECONDS);
        }
        FunctionKt.startExecutor();
    }

    @Override
    public void onDisable() {
        if (pluginInstance != null) {
            pluginInstance.onDisable();
        }
        TabooLibCommon.cancel();
    }

    @NotNull
    public static BungeePlugin getInstance() {
        return instance;
    }
}
