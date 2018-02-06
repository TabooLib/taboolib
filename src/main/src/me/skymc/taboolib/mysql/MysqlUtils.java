package me.skymc.taboolib.mysql;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import me.skymc.taboolib.Main;
import me.skymc.taboolib.message.MsgUtils;
import me.skymc.taboolib.mysql.protect.MySQLConnection;

public class MysqlUtils {
	
	public final static CopyOnWriteArrayList<MySQLConnection> CONNECTIONS = new CopyOnWriteArrayList<>();
	
	public static MysqlConnection getMysqlConnectionFromConfiguration(FileConfiguration conf, String key) {
		return new MysqlConnection(conf.getString(key + ".host"), conf.getString(key + ".port"), conf.getString(key + ".database"), conf.getString(key + ".user"), conf.getString(key + ".pass"));
	}
	
	public static MySQLConnection getMySQLConnectionFromConfiguration(FileConfiguration conf, String key) {
		return getMySQLConnectionFromConfiguration(conf, key, 60, Main.getInst());
	}
	
	public static MySQLConnection getMySQLConnectionFromConfiguration(FileConfiguration conf, String key, int recheck, Plugin plugin) {
		MySQLConnection conn = new MySQLConnection(
				conf.getString(key + ".url"), 
				conf.getString(key + ".user"), 
				conf.getString(key + ".port"), 
				conf.getString(key + ".password"), 
				conf.getString(key + ".database"), recheck, plugin);
		
		if (conn.isConnection()) {
			CONNECTIONS.add(conn);
			MsgUtils.send("已向书库注册插件 &f" + plugin.getName() + "&7 的数据库连接");
		}
		return conn;
	}
}
