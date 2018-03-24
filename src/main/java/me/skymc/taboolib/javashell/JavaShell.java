package me.skymc.taboolib.javashell;

import lombok.Getter;
import lombok.Setter;
import me.skymc.taboolib.Main;
import me.skymc.taboolib.javashell.utils.JarUtils;
import me.skymc.taboolib.message.MsgUtils;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;

public class JavaShell {
	
	@Getter
	@Setter
	private static String paths = "";
	
	@Getter
	@Setter
	private static File javaShellFolder;
	
	@Getter
	@Setter
	private static File scriptFolder;
	
	@Getter
	@Setter
	private static File cacheFolder;
	
	@Getter
	@Setter
	private static File libFolder;
	
	@Getter
	@Setter
	private static HashMap<String, Class<?>> shells = new HashMap<>();
	
	public static void javaShellSetup() {
		File dataFolder = Main.getInst().getDataFolder();
		File pluginsFolder = dataFolder.getParentFile();
		File serverRoot = Bukkit.getWorldContainer();

		File[] rootJars = serverRoot.listFiles((dir, name) -> name.toLowerCase().endsWith("jar"));

		File[] pluginJars = pluginsFolder.listFiles((dir, name) -> name.toLowerCase().endsWith("jar"));

		for (File file : (File[]) ArrayUtils.addAll(rootJars, pluginJars)) {
			String path = file.getAbsolutePath();
			paths += File.pathSeparator + path;
		}
		
		javaShellFolder = new File(Main.getInst().getDataFolder(), "JavaShells");
		if (!javaShellFolder.exists()) {
			Main.getInst().saveResource("JavaShells/scripts/-testshell.java", true);
		}
		
		scriptFolder = new File(javaShellFolder, "scripts");
		if (!scriptFolder.exists()) {
			scriptFolder.mkdir();
		}
		
		cacheFolder = new File(javaShellFolder, "cache");
		if (!cacheFolder.exists()) {
			cacheFolder.mkdir();
		}
		
		libFolder = new File(javaShellFolder, "lib");
		if (!libFolder.exists()) {
			libFolder.mkdir();
		}
		
		File tools = new File(Main.getInst().getDataFolder(), "JavaShells/lib/com.sun.tools.jar");
		if (!tools.exists()) {
			MsgUtils.warn("&4JavaShell &c工具的必要依赖 &4com.sun.tools.jar &c不存在, 功能关闭!");
			return;
		}
		
		loadLibrary();
		new BukkitRunnable() {
			
			@Override
			public void run() {
				long time = System.currentTimeMillis();
				for (File file : scriptFolder.listFiles()) {
					if (!file.getName().startsWith("-")) {
						reloadShell(file.getName());
					}
				}
				MsgUtils.send("载入 " + shells.size() + " 个脚本, 耗时 &f" + (System.currentTimeMillis() - time) + "ms");
			}
		}.runTask(Main.getInst());
	}
	
	public static void javaShellCancel() {
		for (File cacheFile : cacheFolder.listFiles()) {
			cacheFile.delete();
		}
		for (String name : shells.keySet()) {
			invokeMethod(name, "onDisable");
		}
	}
	
	public static void invokeMethod(String name, String method) {
		if (shells.containsKey(name)) {
			Class<?> clazz = shells.get(name);
			try {
				Method disableMethod = clazz.getMethod(method);
				if (disableMethod != null) {
					disableMethod.invoke(clazz.newInstance());
				}
			}
			catch (Exception e) {
				//
			}
		}
	}
	
	public static void unloadShell(String shell) {
		invokeMethod(shell, "onDisable");
		Class<?> clazz = shells.remove(shell);
		try {
			if (clazz.newInstance() instanceof Listener) {
				for (RegisteredListener listener : HandlerList.getRegisteredListeners(Main.getInst())) {
					if (listener.getListener().getClass().getName().equals(clazz.getName())) {
						HandlerList.unregisterAll(listener.getListener());
					}
				}
				MsgUtils.send("已为脚本 &f" + shell + " &7注销监听器");
			}
		}
		catch (Exception e) {
			//
		}
	}
	
	public static boolean reloadShell(String shell) {
		unloadShell(shell = shell.replace(".java", ""));
		
		try {
			Class.forName("com.sun.tools.javac.main.Main");
		}
		catch (Exception e) {
			MsgUtils.warn("&4JavaShell &c工具的必要依赖 &4com.sun.tools.jar &c丢失, 无法载入!");
			return false;
		}
		
	    File javaFile = new File(scriptFolder, shell + ".java");
	    if (!javaFile.exists()) {
	    	MsgUtils.send("&c脚本 &4" + shell + "&c 不存在");
	    	return false; 
	    }
	    
	    String[] args = {
	    		  "-nowarn",
	    	      "-classpath", "." + File.pathSeparator + JavaShell.getPaths(), 
	    	      "-d", cacheFolder.getAbsolutePath() + File.separator, 
	    	      javaFile.getAbsolutePath() 
	    };
	    
	    int code = new com.sun.tools.javac.main.Main("javac").compile(args).exitCode;
	    if (code == 0) {
	    	MsgUtils.send("&f" + shell + "&7 载入成功");
	    	 try {
	    		 URL[] urls = { cacheFolder.toURI().toURL() };
	    		 URLClassLoader sysloader = new URLClassLoader(urls, Main.class.getClassLoader());
	    		 Class<?> clazz = sysloader.loadClass(shell);
	    		 shells.put(shell, clazz);
	    		 sysloader.close();
	    		 
	    		 invokeMethod(shell, "onEnable");
	    		 if (clazz.newInstance() instanceof Listener) {
	    			 Bukkit.getPluginManager().registerEvents((Listener) clazz.newInstance(), Main.getInst());
	    			 MsgUtils.send("已为脚本 &f" + shell + " &7注册监听器");
	    		 }
	    	 }
	    	 catch (Exception e) {
	    		 //
	    	 }
	    	 return true;
	    }
	    else {
	    	MsgUtils.send("&4" + shell + "&c 载入失败");
	    	return false;
	    }
	}
	
	private static void loadLibrary() {
		for (File jar : libFolder.listFiles()) {
			try {
				JarUtils.addClassPath(JarUtils.getJarUrl(jar));
				MsgUtils.send("成功载入 &f" + jar.getName() + " &7到运行库");
			} catch (Exception e) {
				//
			}
		}
	}
}
