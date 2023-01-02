package taboolib.platform;

import net.md_5.bungee.BungeeCord;
import taboolib.common.LifeCycle;
import taboolib.common.TabooLibCommon;
import taboolib.common.io.Project1Kt;
import taboolib.common.platform.Platform;
import taboolib.common.platform.Plugin;
import taboolib.common.platform.function.ExecutorKt;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import static taboolib.platform.BungeePlugin.getPluginInstance;

@SuppressWarnings({"Convert2Lambda", "DuplicatedCode"})
public class BungeePluginDelegate {

	private final Field pluginInstance;

	public BungeePluginDelegate() throws ClassNotFoundException, NoSuchFieldException {
		this.pluginInstance = Class.forName("taboolib.platform.BungeePlugin").getDeclaredField("pluginInstance");
		this.pluginInstance.setAccessible(true);
	}

	public void onConst() throws IllegalAccessException {
		TabooLibCommon.lifeCycle(LifeCycle.CONST, Platform.BUNGEE);
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
		// 判断插件是否关闭
		if (!TabooLibCommon.isStopped()) {
			// 调用 onEnable() 方法
			if (getPluginInstance() != null) {
				getPluginInstance().onEnable();
			}
			// 启动调度器
			try {
				ExecutorKt.startExecutor();
			} catch (NoClassDefFoundError ignored) {
			}
		}
		// 再次判断插件是否关闭
		if (!TabooLibCommon.isStopped()) {
			// 创建调度器，执行 onActive() 方法
			BungeeCord.getInstance().getScheduler().schedule(BungeePlugin.getInstance(), new Runnable() {
				@Override
				public void run() {
					TabooLibCommon.lifeCycle(LifeCycle.ACTIVE);
					if (getPluginInstance() != null) {
						getPluginInstance().onActive();
					}
				}
			}, 0, TimeUnit.SECONDS);
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
