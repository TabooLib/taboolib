package taboolib.platform;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;
import org.tabooproject.reflex.Reflex;
import taboolib.common.LifeCycle;
import taboolib.common.TabooLibCommon;
import taboolib.common.io.Project1Kt;
import taboolib.common.platform.Platform;
import taboolib.common.platform.Plugin;
import taboolib.common.platform.function.ExecutorKt;

import java.lang.reflect.Field;
import java.util.Set;

public class BukkitPluginDelegate {
	
	private final ClassLoader pluginClassLoader;
	private final Class<?> pluginClass;
	private final Field pluginInstance;
	
	
	public BukkitPluginDelegate(ClassLoader pluginClassLoader) throws ClassNotFoundException, NoSuchFieldException {
		this.pluginClassLoader = pluginClassLoader;
		this.pluginClass = Class.forName("taboolib.platform.BukkitPlugin", false, pluginClassLoader);
		this.pluginInstance = pluginClass.getDeclaredField("pluginInstance");
		
		pluginInstance.setAccessible(true);
	}
	

	public void onConst() throws IllegalAccessException {
		TabooLibCommon.lifeCycle(LifeCycle.CONST, Platform.BUKKIT);
		// 搜索 Plugin 实现
		if (TabooLibCommon.isKotlinEnvironment()) {
			pluginInstance.set(null, Project1Kt.findImplementation(Plugin.class));
		}
	}
	
	public void onInit() {
		// 修改访问提示（似乎有用）
		injectAccess();
		// 生命周期
		TabooLibCommon.lifeCycle(LifeCycle.INIT);
	}
	
	public void onLoad() throws IllegalAccessException {
		TabooLibCommon.lifeCycle(LifeCycle.LOAD);
		// 再次尝试搜索 Plugin 实现
		if (pluginInstance.get(null) == null) {
			pluginInstance.set(null, Project1Kt.findImplementation(Plugin.class));
		}
		// 调用 Plugin 实现的 onLoad() 方法
		Object pluginObj = pluginInstance.get(null);
		if (pluginObj != null && !TabooLibCommon.isStopped()) {
			((Plugin) pluginObj).onLoad();
		}
	}

	public void onEnable() throws IllegalAccessException {
		TabooLibCommon.lifeCycle(LifeCycle.ENABLE);
		// 判断插件是否关闭
		if (!TabooLibCommon.isStopped()) {
			// 调用 onEnable() 方法
			Object pluginObj = pluginInstance.get(null);
			if (pluginObj != null) {
				((Plugin) pluginObj).onEnable();
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
			Bukkit.getScheduler().runTask(BukkitPlugin.getInstance(), new Runnable() {
				@Override
				public void run() {
					TabooLibCommon.lifeCycle(LifeCycle.ACTIVE);
					Object pluginObj;
					try {
						pluginObj = pluginInstance.get(null);
					} catch (IllegalAccessException ex) {
						throw new RuntimeException(ex);
					}
					if (pluginObj != null) {
						((Plugin) pluginObj).onActive();
					}
				}
			});
		}
	}

	public void onDisable() throws IllegalAccessException {
		TabooLibCommon.lifeCycle(LifeCycle.DISABLE);
		// 在插件未关闭的前提下，执行 onDisable() 方法
		Object pluginObj = pluginInstance.get(null);
		if (pluginObj != null && !TabooLibCommon.isStopped()) {
			((Plugin) pluginObj).onDisable();
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
