package taboolib.platform;

import cn.nukkit.Server;
import cn.nukkit.plugin.PluginBase;
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
 * taboolib.platform.NukkitPlugin
 *
 * @author sky
 * @since 2021/6/26 8:22 下午
 */
@SuppressWarnings({"Convert2Lambda", "FieldCanBeLocal"})
@PlatformSide(Platform.NUKKIT)
public class NukkitPlugin extends PluginBase {

    @Nullable
    private static Plugin pluginInstance;
    private static NukkitPlugin instance;

    static {
        TabooLib.booster().proceed(LifeCycle.CONST, Platform.NUKKIT);
        if (TabooLib.isKotlinEnvironment()) {
            pluginInstance = ClassInstanceKt.findInstanceFromPlatform(Plugin.class);
        }
    }

    public NukkitPlugin() {
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
            Server.getInstance().getScheduler().scheduleTask(this, new Runnable() {
                @Override
                public void run() {
                    TabooLib.booster().proceed(LifeCycle.ACTIVE);
                    if (pluginInstance != null) {
                        pluginInstance.onActive();
                    }
                }
            });
        }
    }

    @Override
    public void onDisable() {
        TabooLib.booster().proceed(LifeCycle.DISABLE);
        if (pluginInstance != null && !TabooLib.isStopped()) {
            pluginInstance.onDisable();
        }
    }

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
        return pluginInstance;
    }
}
