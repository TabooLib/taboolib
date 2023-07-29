package taboolib.platform;

import cn.nukkit.Server;
import taboolib.common.LifeCycle;
import taboolib.common.TabooLibCommon;
import taboolib.common.io.Project1Kt;
import taboolib.common.platform.Platform;
import taboolib.common.platform.Plugin;
import taboolib.common.platform.function.ExecutorKt;

import java.lang.reflect.Field;

public class NukkitPluginDelegate {

	private final Class<?> pluginClass;
	private final Field pluginInstance;


	public NukkitPluginDelegate() throws ClassNotFoundException, NoSuchFieldException {
		this.pluginClass = Class.forName("taboolib.platform.NukkitPlugin");
		this.pluginInstance = pluginClass.getDeclaredField("pluginInstance");

		pluginInstance.setAccessible(true);
	}


	public void onConst() throws IllegalAccessException {
		TabooLibCommon.lifeCycle(LifeCycle.CONST, Platform.NUKKIT);
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
		if (NukkitPlugin.getPluginInstance() == null) {
			pluginInstance.set(null, Project1Kt.findImplementation(Plugin.class));
		}
		// 调用 Plugin 实现的 onLoad() 方法
		if (NukkitPlugin.getPluginInstance() != null && !TabooLibCommon.isStopped()) {
			NukkitPlugin.getPluginInstance().onLoad();
		}
	}

	public void onEnable() {
		TabooLibCommon.lifeCycle(LifeCycle.ENABLE);
		// 判断插件是否关闭
		if (!TabooLibCommon.isStopped()) {
			// 调用 onEnable() 方法
			if (NukkitPlugin.getPluginInstance() != null) {
				NukkitPlugin.getPluginInstance().onEnable();
			}
			// 启动调度器
			try {
				ExecutorKt.startExecutor();
			} catch (NoClassDefFoundError ignored) {
			}
		}
		// 再次判断插件是否关闭
		// 因为插件可能在 onEnable() 下关闭
		if (!TabooLibCommon.isStopped()) {
			// 创建调度器，执行 onActive() 方法
			Server.getInstance().getScheduler().scheduleTask(NukkitPlugin.getInstance(), new Runnable() {
				@Override
				public void run() {
					TabooLibCommon.lifeCycle(LifeCycle.ACTIVE);
					if (NukkitPlugin.getPluginInstance() != null) {
						NukkitPlugin.getPluginInstance().onActive();
					}
				}
			});
		}
	}

	public void onDisable() {
		TabooLibCommon.lifeCycle(LifeCycle.DISABLE);
		// 在插件未关闭的前提下，执行 onDisable() 方法
		if (NukkitPlugin.getPluginInstance() != null && !TabooLibCommon.isStopped()) {
			NukkitPlugin.getPluginInstance().onDisable();
		}
	}
	
}
