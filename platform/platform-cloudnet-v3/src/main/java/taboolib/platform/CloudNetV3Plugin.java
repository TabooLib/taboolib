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
    @Nullable
    private static IsolatedClassLoader isolatedClassLoader;

    static {
	    if (IsolatedClassLoader.isEnabled()) {
	        try {
	            IsolatedClassLoader loader = new IsolatedClassLoader(
	                    new URL[]{CloudNetV3Plugin.class.getProtectionDomain().getCodeSource().getLocation()},
	                    CloudNetV3Plugin.class.getClassLoader()
	            );
	            loader.addExcludedClass("taboolib.platform.CloudNetV3Plugin");
	            isolatedClassLoader = loader;
	            delegateClass = Class.forName("taboolib.platform.CloudNetV3PluginDelegate", true, loader);
	            delegateObject = delegateClass.getConstructor().newInstance();
	            delegateClass.getMethod("onConst").invoke(delegateObject);
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	    } else {
	        TabooLibCommon.lifeCycle(LifeCycle.CONST, Platform.BUNGEE);
	        if (TabooLibCommon.isKotlinEnvironment()) {
	            pluginInstance = Project1Kt.findImplementation(Plugin.class);
	        }
	    }
    }

    public CloudNetV3Plugin() {
        instance = this;

	    if (IsolatedClassLoader.isEnabled()) {
	        try {
	            delegateClass.getMethod("onInit").invoke(delegateObject);
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	    } else {
	        TabooLibCommon.lifeCycle(LifeCycle.INIT);
	    }
    }

    @ModuleTask(event = ModuleLifeCycle.LOADED)
    public void onLoad() {
	    if (IsolatedClassLoader.isEnabled()) {
	        try {
	            delegateClass.getMethod("onLoad").invoke(delegateObject);
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	    } else {
	        TabooLibCommon.lifeCycle(LifeCycle.LOAD);
	        if (pluginInstance == null) {
	            pluginInstance = Project1Kt.findImplementation(Plugin.class);
	        }
	        if (pluginInstance != null && !TabooLibCommon.isStopped()) {
	            pluginInstance.onLoad();
	        }
	    }
    }

    @ModuleTask(event = ModuleLifeCycle.STARTED)
    public void onEnable() {
	    if (IsolatedClassLoader.isEnabled()) {
	        try {
	            delegateClass.getMethod("onEnable").invoke(delegateObject);
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	    } else {
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
    }

    @ModuleTask(event = ModuleLifeCycle.STOPPED)
    public void onDisable() {
	    if (IsolatedClassLoader.isEnabled()) {
	        try {
	            delegateClass.getMethod("onDisable").invoke(delegateObject);
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	    } else {
	        TabooLibCommon.lifeCycle(LifeCycle.DISABLE);
	        if (pluginInstance != null && !TabooLibCommon.isStopped()) {
	            pluginInstance.onDisable();
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

    @Nullable
    public static IsolatedClassLoader getIsolatedClassLoader() {
        return isolatedClassLoader;
    }
}
