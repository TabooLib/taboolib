package taboolib.platform;

import de.dytanic.cloudnet.CloudNet;
import de.dytanic.cloudnet.driver.module.ModuleLifeCycle;
import de.dytanic.cloudnet.driver.module.ModuleTask;
import de.dytanic.cloudnet.driver.module.driver.DriverModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import taboolib.common.LifeCycle;
import taboolib.common.TabooLibCommon;
import taboolib.common.env.IsolatedClassLoader;
import taboolib.common.io.Project1Kt;
import taboolib.common.platform.Platform;
import taboolib.common.platform.PlatformSide;
import taboolib.common.platform.Plugin;
import taboolib.common.platform.function.ExecutorKt;

import java.io.File;
import java.net.URL;
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
    private static Class<?> delegateClass;
    private static Object delegateObject;

    static {
        if (!IsolatedClassLoader.isEnabled()) {
            TabooLibCommon.lifeCycle(LifeCycle.CONST, Platform.BUNGEE);
            if (TabooLibCommon.isKotlinEnvironment()) {
                pluginInstance = Project1Kt.findImplementation(Plugin.class);
            }
        } else {
            try {
                IsolatedClassLoader loader = new IsolatedClassLoader(
                        new URL[]{CloudNetV3Plugin.class.getProtectionDomain().getCodeSource().getLocation()},
                        CloudNetV3Plugin.class.getClassLoader()
                );
                delegateClass = Class.forName("taboolib.platform.CloudNetV3PluginDelegate", true, loader);
                delegateObject = delegateClass.getConstructor().newInstance();
                delegateClass.getMethod("onConst").invoke(delegateObject);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public CloudNetV3Plugin() {
        instance = this;
        
        if (!IsolatedClassLoader.isEnabled()) {
            TabooLibCommon.lifeCycle(LifeCycle.INIT);
        } else {
            try {
                delegateClass.getMethod("onInit").invoke(delegateObject);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @ModuleTask(event = ModuleLifeCycle.LOADED)
    public void onLoad() {
        if (!IsolatedClassLoader.isEnabled()) {
            TabooLibCommon.lifeCycle(LifeCycle.LOAD);
            if (pluginInstance == null) {
                pluginInstance = Project1Kt.findImplementation(Plugin.class);
            }
            if (pluginInstance != null && !TabooLibCommon.isStopped()) {
                pluginInstance.onLoad();
            }
        } else {
            try {
                delegateClass.getMethod("onLoad").invoke(delegateObject);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @ModuleTask(event = ModuleLifeCycle.STARTED)
    public void onEnable() {
        if (!IsolatedClassLoader.isEnabled()) {
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
        } else {
            try {
                delegateClass.getMethod("onEnable").invoke(delegateObject);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @ModuleTask(event = ModuleLifeCycle.STOPPED)
    public void onDisable() {
        if (!IsolatedClassLoader.isEnabled()) {
            TabooLibCommon.lifeCycle(LifeCycle.DISABLE);
            if (pluginInstance != null && !TabooLibCommon.isStopped()) {
                pluginInstance.onDisable();
            }
        } else {
            try {
                delegateClass.getMethod("onDisable").invoke(delegateObject);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
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
