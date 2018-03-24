package me.skymc.taboolib.javascript;

import me.skymc.taboolib.Main;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class JavaScriptUtils {
	
	private static ScriptEngineManager manager = new ScriptEngineManager();
	
	public static ScriptEngineManager getScriptManager() {
		return manager;
	}
	
	public static void invokeJavaScript(File jsFile, String method, Object... o) {
		ScriptEngine engine = manager.getEngineByName("javascript");
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Thread.currentThread().setContextClassLoader(Main.class.getClassLoader());
			
			FileReader reader = new FileReader(jsFile);
			engine.eval(reader);
			
			// TODO run
			
			Thread.currentThread().setContextClassLoader(classLoader);
		}
		catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	@Deprecated
	public static Object JavaScriptInterface(String jsFile, Object... o) {
		
		ScriptEngine engine = manager.getEngineByName("javascript");
		try {
			FileReader reader = new FileReader(jsFile);
			engine.eval(reader);
			
			if (engine instanceof Invocable) {
				return ((Invocable) engine).invokeFunction("main", o);
			}
			
			reader.close();
		} catch (NoSuchMethodException | IOException | ScriptException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Deprecated
	public static void JavaScriptExecute(String jsFile, Object... o) {
		
		ScriptEngine engine = manager.getEngineByName("javascript");
		try {
			FileReader reader = new FileReader(jsFile);
			engine.eval(reader);
			
			if (engine instanceof Invocable) {
				((Invocable) engine).invokeFunction("main", o);
			}
			
			reader.close();
		} catch (NoSuchMethodException | IOException | ScriptException e) {
			e.printStackTrace();
		}
	}

}
