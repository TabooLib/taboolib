package com.ilummc.tlib.resources;

import java.lang.reflect.Field;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import com.ilummc.tlib.TLib;
import com.ilummc.tlib.util.Ref;

import me.skymc.taboolib.Main;

public final class TLocale {

    private TLocale() {
        throw new AssertionError();
    }
    
    private static JavaPlugin getCallerPlugin(Class<?> callerClass) {
    	try {
    		Field pluginField = callerClass.getClassLoader().getClass().getDeclaredField("plugin");
            pluginField.setAccessible(true);
            return (JavaPlugin) pluginField.get(callerClass.getClassLoader());
    	} catch (Exception ignored) {
    		TLib.getTLib().getLogger().error("无效的语言文件发送形式: &4" + callerClass.getName());
    	}
    	return (JavaPlugin) Main.getInst();
    }

    private static void sendTo(String path, CommandSender sender, String[] args, Class<?> callerClass) {
    	TLocaleLoader.sendTo(getCallerPlugin(callerClass), path, sender, args);
    }
    
    private static String asString(String path, String[] args, Class<?> callerClass) {
    	return TLocaleLoader.asString(getCallerPlugin(callerClass), path);
    }
    
    public static void sendToConsole(String path, String... args) {
        Ref.getCallerClass(3).ifPresent(clazz -> sendTo(path, Bukkit.getConsoleSender(), args, clazz));
    }

    public static void sendTo(CommandSender sender, String path, String... args) {
        Ref.getCallerClass(3).ifPresent(clazz -> sendTo(path, sender, args, clazz));
    }
    
    public static void sendTo(String path, CommandSender sender, String... args) {
        Ref.getCallerClass(3).ifPresent(clazz -> sendTo(path, sender, args, clazz));
    }

    public static String asString(String path, String... args) {
    	try {
    		return asString(path, args, Ref.getCallerClass(3).get());
    	} catch (Exception e) {
    		TLib.getTLib().getLogger().error("语言文件获取失败: " + path);
    		TLib.getTLib().getLogger().error("原因: " + e.getMessage());
    		return "§4<" + path + "§4>"; 
    	}
    }
    
    public static void reload() {
    	Ref.getCallerClass(3).ifPresent(clazz -> TLocaleLoader.load(getCallerPlugin(clazz), false));
    }
}
