package taboolib.platform;

import cn.nukkit.Server;
import cn.nukkit.plugin.PluginBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import taboolib.common.TabooLibCommon;
import taboolib.common.io.IOKt;
import taboolib.common.platform.FunctionKt;
import taboolib.common.platform.Platform;
import taboolib.common.platform.PlatformSide;
import taboolib.common.platform.Plugin;

import java.io.File;

/**
 * TabooLib
 * taboolib.platform.NukkitPlugin
 *
 * @author sky
 * @since 2021/6/26 8:22 下午
 */
@SuppressWarnings({"Anonymous2MethodRef", "Convert2Lambda", "FieldCanBeLocal"})
@PlatformSide(Platform.NUKKIT)
public class NukkitPlugin extends PluginBase {

    @Nullable
    private static final Plugin pluginInstance;
    private static NukkitPlugin instance;

    static {
        TabooLibCommon.init();
        pluginInstance = IOKt.findInstance(Plugin.class);
    }

    public NukkitPlugin() {
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
            Server.getInstance().getScheduler().scheduleTask(this, new Runnable() {
                @Override
                public void run() {
                    pluginInstance.onActive();
                }
            });
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

    @Override
    public File getFile() {
        return super.getFile();
    }

    @NotNull
    public static NukkitPlugin getInstance() {
        return instance;
    }
}
