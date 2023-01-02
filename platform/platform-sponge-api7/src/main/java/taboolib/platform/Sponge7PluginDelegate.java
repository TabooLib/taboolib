package taboolib.platform;

import taboolib.common.LifeCycle;
import taboolib.common.TabooLibCommon;
import taboolib.common.io.Project1Kt;
import taboolib.common.platform.Platform;
import taboolib.common.platform.Plugin;
import taboolib.common.platform.function.ExecutorKt;

import java.lang.reflect.Field;

import static taboolib.platform.Sponge7Plugin.getPluginInstance;

public class Sponge7PluginDelegate {

	private final Class<?> pluginClass;
	private final Field pluginInstance;


	public Sponge7PluginDelegate() throws ClassNotFoundException, NoSuchFieldException {
		this.pluginClass = Class.forName("taboolib.platform.Sponge7Plugin");
		this.pluginInstance = pluginClass.getDeclaredField("pluginInstance");

		pluginInstance.setAccessible(true);
	}


	public void onConst() throws IllegalAccessException {
		TabooLibCommon.lifeCycle(LifeCycle.CONST, Platform.SPONGE_API_7);
		// 搜索 Plugin 实现
		if (TabooLibCommon.isKotlinEnvironment()) {
			pluginInstance.set(null, Project1Kt.findImplementation(Plugin.class));
		}
	}

	public void onInit() {
		// 生命周期
		TabooLibCommon.lifeCycle(LifeCycle.INIT);
	}

	public void onLoad() throws IllegalAccessException {
		TabooLibCommon.lifeCycle(LifeCycle.LOAD);
		// 再次尝试搜索 Plugin 实现
		if (getPluginInstance() == null) {
			pluginInstance.set(null, Project1Kt.findImplementation(Plugin.class));
		}
		// 调用 Plugin 实现的 onLoad() 方法
		if (getPluginInstance() != null && !TabooLibCommon.isStopped()) {
			getPluginInstance().onLoad();
		}
	}

	public void onEnable() {
		TabooLibCommon.lifeCycle(LifeCycle.ENABLE);
		if (!TabooLibCommon.isStopped()) {
			if (getPluginInstance() != null) {
				getPluginInstance().onEnable();
			}
			try {
				ExecutorKt.startExecutor();
			} catch (NoClassDefFoundError ignored) {
			}
		}
	}
	
	public void onActive() {
		TabooLibCommon.lifeCycle(LifeCycle.ACTIVE);
		if (getPluginInstance() != null && !TabooLibCommon.isStopped()) {
			getPluginInstance().onActive();
		}
	}

	public void onDisable() {
		TabooLibCommon.lifeCycle(LifeCycle.DISABLE);
		// 在插件未关闭的前提下，执行 onDisable() 方法
		if (getPluginInstance() != null && !TabooLibCommon.isStopped()) {
			getPluginInstance().onDisable();
		}
	}
	
}
