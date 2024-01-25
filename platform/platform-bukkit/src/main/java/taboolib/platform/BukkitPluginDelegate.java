package taboolib.platform;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;
import org.tabooproject.reflex.Reflex;
import taboolib.common.LifeCycle;
import taboolib.common.TabooLib;
import taboolib.common.io.Project1Kt;
import taboolib.common.platform.Platform;
import taboolib.common.platform.Plugin;
import taboolib.common.platform.function.ExecutorKt;

import java.lang.reflect.Field;
import java.util.Set;

import static taboolib.platform.BukkitPlugin.getPluginInstance;

@SuppressWarnings({"Convert2Lambda", "DuplicatedCode"})
public class BukkitPluginDelegate {

	private final Field pluginInstance;
	
	public BukkitPluginDelegate() throws ClassNotFoundException, NoSuchFieldException {
		this.pluginInstance = Class.forName("taboolib.platform.BukkitPlugin").getDeclaredField("pluginInstance");
		this.pluginInstance.setAccessible(true);
	}


	public void onConst() throws IllegalAccessException {
		TabooLib.lifeCycle(LifeCycle.CONST, Platform.BUKKIT);
		// 搜索 Plugin 实现
		if (TabooLib.isKotlinEnvironment()) {
			pluginInstance.set(null, Project1Kt.findImplementation(Plugin.class));
		}
	}
	
	public void onInit() {
		// 修改访问提示（似乎有用）
		injectAccess();
		// 生命周期
		TabooLib.lifeCycle(LifeCycle.INIT);
	}
	
	public void onLoad() throws IllegalAccessException {
		TabooLib.lifeCycle(LifeCycle.LOAD);
		// 再次尝试搜索 Plugin 实现
		if (getPluginInstance() == null) {
			pluginInstance.set(null, Project1Kt.findImplementation(Plugin.class));
		}
		// 调用 Plugin 实现的 onLoad() 方法
		if (getPluginInstance() != null && !TabooLib.isStopped()) {
			getPluginInstance().onLoad();
		}
	}

	public void onEnable() {
		TabooLib.lifeCycle(LifeCycle.ENABLE);
		// 判断插件是否关闭
		if (!TabooLib.isStopped()) {
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
		// 因为插件可能在 onEnable() 下关闭
		if (!TabooLib.isStopped()) {
			// 创建调度器，执行 onActive() 方法
			Bukkit.getScheduler().runTask(BukkitPlugin.getInstance(), new Runnable() {
				@Override
				public void run() {
					TabooLib.lifeCycle(LifeCycle.ACTIVE);
					if (getPluginInstance() != null) {
						getPluginInstance().onActive();
					}
				}
			});
		}
	}

	public void onDisable() {
		TabooLib.lifeCycle(LifeCycle.DISABLE);
		// 在插件未关闭的前提下，执行 onDisable() 方法
		if (getPluginInstance() != null && !TabooLib.isStopped()) {
			getPluginInstance().onDisable();
		}
	}

	/**
	 * 移除 Spigot 的访问警告：
	 * Loaded class {0} from {1} which is not a depend, softdepend or loadbefore of this plugin
	 */
	@SuppressWarnings("DataFlowIssue")
	static void injectAccess() {
		try {
			PluginDescriptionFile description = Reflex.Companion.getProperty(BukkitPlugin.class.getClassLoader(), "description", false, true, false);
			Set<String> accessSelf = Reflex.Companion.getProperty(BukkitPlugin.class.getClassLoader(), "seenIllegalAccess", false, true, false);
			for (org.bukkit.plugin.Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
				if (plugin.getClass().getName().endsWith("platform.BukkitPlugin")) {
					Set<String> accessOther = Reflex.Companion.getProperty(plugin.getClass().getClassLoader(), "seenIllegalAccess", false, true, false);
					accessOther.add(description.getName());
					accessSelf.add(plugin.getName());
				}
			}
		} catch (Throwable ignored) {
		}
	}
}
