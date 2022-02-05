package taboolib.platform;

import de.dytanic.cloudnet.CloudNet;
import de.dytanic.cloudnet.driver.module.ModuleLifeCycle;
import de.dytanic.cloudnet.driver.module.ModuleTask;
import de.dytanic.cloudnet.driver.module.driver.DriverModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import taboolib.common.LifeCycle;
import taboolib.common.TabooLib;
import taboolib.common.boot.Environments;
import taboolib.common.io.ClassInstanceKt;
import taboolib.common.platform.Platform;
import taboolib.common.platform.PlatformSide;
import taboolib.common.platform.Plugin;

/**
 * TabooLib
 * taboolib.platform.BungeePlugin
 *
 * @author sky
 * @since 2021/6/26 8:22 下午
 */
@PlatformSide(Platform.CLOUDNET_V3)
public class CloudNetV3Plugin extends DriverModule {

    @Nullable
    private static Plugin instanceDelegate;
    private static CloudNetV3Plugin instance;

    static {
        TabooLib.booster().proceed(LifeCycle.CONST, Platform.CLOUDNET_V3);
        setupPluginInstance();
    }

    public CloudNetV3Plugin() {
        instance = this;
        TabooLib.booster().proceed(LifeCycle.INIT);
    }

    @ModuleTask(event = ModuleLifeCycle.LOADED)
    public void onLoad() {
        TabooLib.booster().proceed(LifeCycle.LOAD);
        setupPluginInstance();
        if (isRunning() && instanceDelegate != null) {
            instanceDelegate.onLoad();
        }
    }

    @ModuleTask(event = ModuleLifeCycle.STARTED)
    public void onEnable() {
        TabooLib.booster().proceed(LifeCycle.ENABLE);
        if (isRunning() && instanceDelegate != null) {
            instanceDelegate.onEnable();
        }
        if (isRunning()) {
            CloudNet.getInstance().scheduleTask(() -> {
                TabooLib.booster().proceed(LifeCycle.ACTIVE);
                if (instanceDelegate != null) {
                    instanceDelegate.onActive();
                }
                return null;
            });
        }
    }

    @ModuleTask(event = ModuleLifeCycle.STOPPED)
    public void onDisable() {
        TabooLib.booster().proceed(LifeCycle.DISABLE);
        if (instanceDelegate != null && isRunning()) {
            instanceDelegate.onDisable();
        }
    }

    @NotNull
    public static CloudNetV3Plugin getInstance() {
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
