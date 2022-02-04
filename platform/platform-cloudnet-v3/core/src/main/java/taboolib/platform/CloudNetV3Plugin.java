package taboolib.platform;

import de.dytanic.cloudnet.CloudNet;
import de.dytanic.cloudnet.driver.module.ModuleLifeCycle;
import de.dytanic.cloudnet.driver.module.ModuleTask;
import de.dytanic.cloudnet.driver.module.driver.DriverModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import taboolib.common.LifeCycle;
import taboolib.common.TabooLibCommon;
import taboolib.common.io.Project1Kt;
import taboolib.common.platform.Platform;
import taboolib.common.platform.PlatformSide;
import taboolib.common.platform.Plugin;
import taboolib.common.platform.function.ExecutorKt;

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
public class CloudNetV3Plugin extends DriverModule {

    @Nullable
    private static Plugin pluginInstance;
    private static CloudNetV3Plugin instance;

    static {
        TabooLibCommon.lifeCycle(LifeCycle.CONST, Platform.BUNGEE);
        if (TabooLibCommon.isKotlinEnvironment()) {
            pluginInstance = Project1Kt.findImplementation(Plugin.class);
        }
    }

    public CloudNetV3Plugin() {
        instance = this;
        TabooLibCommon.lifeCycle(LifeCycle.INIT);
    }

    @ModuleTask(event = ModuleLifeCycle.LOADED)
    public void onLoad() {
        TabooLibCommon.lifeCycle(LifeCycle.LOAD);
        if (pluginInstance == null) {
            pluginInstance = Project1Kt.findImplementation(Plugin.class);
        }
        if (pluginInstance != null && !TabooLibCommon.isStopped()) {
            pluginInstance.onLoad();
        }
    }

    @ModuleTask(event = ModuleLifeCycle.STARTED)
    public void onEnable() {
        TabooLibCommon.lifeCycle(LifeCycle.ENABLE);
        if (!TabooLibCommon.isStopped()) {
            if (pluginInstance != null) {
                pluginInstance.onEnable();
            }
            try {
                ExecutorKt.startExecutor();
            } catch (NoClassDefFoundError ignored) {
            }
        }
        if (!TabooLibCommon.isStopped()) {
            CloudNet.getInstance().scheduleTask(() -> {
                TabooLibCommon.lifeCycle(LifeCycle.ACTIVE);
                if (pluginInstance != null) {
                    pluginInstance.onActive();
                }
                return null;
            });
        }
    }

    @ModuleTask(event = ModuleLifeCycle.STOPPED)
    public void onDisable() {
        TabooLibCommon.lifeCycle(LifeCycle.DISABLE);
        if (pluginInstance != null && !TabooLibCommon.isStopped()) {
            pluginInstance.onDisable();
        }
    }

    @NotNull
    public static CloudNetV3Plugin getInstance() {
        return instance;
    }

    @Nullable
    public static Plugin getPluginInstance() {
        return pluginInstance;
    }
}
