package me.skymc.taboolib.mysql.protect;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.plugin.Plugin;

import me.skymc.taboolib.Main;

public class MySQLConnection {
	
	private String url;
	private String user;
	private String port;
	private String password;
	private String database;
	private String connectionUrl;
	private Connection connection = null;
	
	private int recheck = 10;
	private Thread recheckThread;
	
	private Plugin plugin;
	
	public MySQLConnection(String url, String user, String port, String password, String database) {
		this(url, user, port, password, database, 10, Main.getInst());
	}
	
	public MySQLConnection(String url, String user, String port, String password, String database, int recheck, Plugin plugin) {
		// 检查驱动
		if (!loadDriverMySQL()) {
			print("驱动器获取失败, 无法连接到数据库");
			return;
		}
		
		// 设置信息
		this.plugin = plugin;
		this.recheck = recheck;
		
		// 设置数据
		this.url = url == null ? "localhost" : url;
		this.user = user == null ? "root" : user;
		this.port = port == null ? "3306" : port;
		this.password = password == null ? "" : password;
		this.database = database == null ? "test" : database;
		this.connectionUrl = "jdbc:mysql://" + this.url + ":" + this.port + "/" + this.database + "?characterEncoding=utf-8&useSSL=false";
		
		// 连接数据库
		connect();
		
		// 断线检测
		recheckThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while (!Main.isDisable()) {
					try {
						Thread.sleep(getReCheckSeconds() * 1000);
						
						if (connection == null) {
							print("警告! 数据库尚未连接, 请检查配置文件后重启服务器! (" + (plugin.getName()) + ")");
							continue;
						}
						else {
							isExists("taboolib");
						}
					} catch (Exception e) {
						print("数据库命令执行出错");
						print("错误原因: " + e.getMessage());
					}
				}
			}
		});
		
		// 启动检测
		if (isConnection()) {
			recheckThread.start(); 
			print("启动数据库连接监控");
		}
	}
	
	public Plugin getPlugin() {
		return this.plugin;
	}
	
	public void setPlugin(Plugin plugin) {
		this.plugin = plugin;
	}
	
	public void setReCheckSeconds(int s) {
		this.recheck = s;
	}
	
	public int getReCheckSeconds() {
		return recheck;
	}
	
	public boolean isConnection() {
		try {
			if (connection == null || connection.isClosed()) {
				return false;
			}
		} catch (SQLException e) {
			return false;
		}
		return true;
	}
	
	@SuppressWarnings("deprecation")
	public void closeConnection() {
		try {
			connection.close();
			recheckThread.stop();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
		}
	}
	
	public boolean deleteTable(String name) {
		return execute("drop table if exists " + name);
	}
	
	/**
	 * 2018年1月17日 新增, TabooLib 版本 3.25
	 */
	public boolean truncateTable(String name) {
		return execute("truncate table " + name);
	}
	
	public boolean clearTable(String name) {
		return execute("delete from " + name);
	}
	
	public boolean renameTable(String name, String newName) {
		return execute("rename table `" + name + "` to `" + newName + "`");
	}
	
	public boolean deleteColumn(String name, String column) {
		return execute("alter table `" + name + "` drop `" + column + "`");
	}
	
	public boolean deleteValue(String name, String column, Object columnValue) {
		return execute("delete from `" + name + "` where `" + column + "` = " + parseToString(columnValue));
	}
	
	public boolean setValue(String name, String column, Object columnValue, String valueColumn, Object value) {
		return setValue(name, column, columnValue, valueColumn, value, false);
	}
	
	public boolean setValue(String name, String column, Object columnValue, String valueColumn, Object value, boolean append) {
		if (!append) {
			return execute("update `" + name + "` set `" + valueColumn + "` = " + parseToString(value) + " where `" + column + "` = " + parseToString(columnValue));
		}
		else {
			return execute("update `" + name + "` set `" + valueColumn + "` = " + valueColumn + " + " + parseToString(value) + " where `" + column + "` = " + parseToString(columnValue));
		}
	}
	
	public void addColumn(String name, Column... columns) {
		for (Column column : columns) {
			execute("alter table " + name + " add " + column.toString());
		}
	}
	
	public boolean addColumn(String name, String column) {
		if (!column.contains("/")) {
			return execute("alter table " + name + " add `" + column + "` text");
		}
		return execute("alter table " + name + " add `" + column.split("/")[0] + "` " + column.split("/")[1]);
	}
	
	public boolean editColumn(String name, String oldColumn, Column newColumn) {
		return execute("alter table " + name + " change `" + oldColumn + "` " + newColumn.toString());
	}
	
	public boolean editColumn(String name, String oldColumn, String newColumn) {
		if (!newColumn.contains("/")) {
			return execute("alter table " + name + " change `" + oldColumn + "` `" + newColumn + "` text");
		}
		return execute("alter table " + name + " change `" + oldColumn + "` `" + newColumn.split("/")[0] + "` " + newColumn.split("/")[1]);
	}
	
	public boolean intoValue(String name, Object... values) {
		StringBuilder sb = new StringBuilder();
		for (Object value : values) {
			sb.append(parseToString(value) + ", ");
		}
		return execute("insert into " + name + " values(null, " + sb.substring(0, sb.length() - 2) + ")");
	}
	
	public boolean createTable(String name, Column... columns) {
		StringBuilder sb = new StringBuilder();
		for (Column column : columns) {
			sb.append(column.toString() + ", ");
		}
		return execute("create table if not exists " + name + " (id int(1) not null primary key auto_increment, " + sb.substring(0, sb.length() - 2) + ")");
	}
	
	public boolean createTable(String name, String... columns) {
		StringBuilder sb = new StringBuilder();
		for (String column : columns) {
			if (!column.contains("/")) {
				sb.append("`" + column + "` text, ");
			}
			else {
				sb.append("`" + column.split("/")[0] + "` " + column.split("/")[1] + ", ");
			}
		}
		return execute("create table if not exists " + name + " (id int(1) not null primary key auto_increment, " + sb.substring(0, sb.length() - 2) + ")");
	}
	
	public boolean isExists(String name) {
		try {
			PreparedStatement pstmt = connection.prepareStatement("select table_name FROM information_schema.TABLES where table_name = " + parseToString(name));
			ResultSet resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				return true;
			}
			resultSet.close();
			pstmt.close();
		}
		catch (Exception e) {
			print("数据库命令执行出错");
			print("错误原因: " + e.getMessage());
			if (e.getMessage().contains("closed")) {
				connect();
			}
		}
		return false;
	}
	
	public boolean isExists(String name, String column, Object columnValue) {
		try {
			PreparedStatement pstmt = connection.prepareStatement("select * from `" + name + "` where `" + column + "` = " + parseToString(columnValue));
			ResultSet resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				return true;
			}
			resultSet.close();
			pstmt.close();
		}
		catch (Exception e) {
			print("数据库命令执行出错");
			print("错误原因: " + e.getMessage());
			if (e.getMessage().contains("closed")) {
				connect();
			}
		}
		return false;
	}
	
	public List<String> getColumns(String name) {
		return getColumns(name, false);
	}
	
	public List<String> getColumns(String name, boolean primary) {
		List<String> list = new ArrayList<>();
		try {
			PreparedStatement pstmt = connection.prepareStatement("select column_name from information_schema.COLUMNS where table_name = " + parseToString(name));
			ResultSet resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				list.add(resultSet.getString(1));
			}
			resultSet.close();
			pstmt.close();
		}
		catch (Exception e) {
			print("数据库命令执行出错");
			print("错误原因: " + e.getMessage());
			if (e.getMessage().contains("closed")) {
				connect();
			}
		}
		if (!primary) {
			list.remove("id");
		}
		return list;
	}
	
	public Object getValue(String name, String column, Object columnValue, String valueColumn) {
		try {
			PreparedStatement pstmt = connection.prepareStatement("select * from `" + name + "` where `" + column + "` = " + parseToString(columnValue) + " limit 1");
			ResultSet resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				return resultSet.getObject(valueColumn);
			}
			resultSet.close();
			pstmt.close();
		}
		catch (Exception e) {
			print("数据库命令执行出错");
			print("错误原因: " + e.getMessage());
			if (e.getMessage().contains("closed")) {
				connect();
			}
		}
		return null;
	}
	
	public Object getValueLast(String name, String column, Object columnValue, String valueColumn) {
		try {
			PreparedStatement pstmt = connection.prepareStatement("select * from `" + name + "` where `" + column + "` = " + parseToString(columnValue) + " order by id desc limit 1");
			ResultSet resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				return resultSet.getObject(valueColumn);
			}
			resultSet.close();
			pstmt.close();
		}
		catch (Exception e) {
			print("数据库命令执行出错");
			print("错误原因: " + e.getMessage());
			if (e.getMessage().contains("closed")) {
				connect();
			}
		}
		return null;
	}
	
	public HashMap<String, Object> getValueLast(String name, String column, Object columnValue, String... valueColumn) {
		HashMap<String, Object> map = new HashMap<>();
		try {
			PreparedStatement pstmt = connection.prepareStatement("select * from `" + name + "` where `" + column + "` = " + parseToString(columnValue) + " order by id desc limit 1");
			ResultSet resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				for (String _column : valueColumn) {
					map.put(_column, resultSet.getObject(_column));
				}
				break;
			}
			resultSet.close();
			pstmt.close();
		}
		catch (Exception e) {
			print("数据库命令执行出错");
			print("错误原因: " + e.getMessage());
			if (e.getMessage().contains("closed")) {
				connect();
			}
		}
		return map;
	}
	
	public HashMap<String, Object> getValue(String name, String column, Object columnValue, String... valueColumn) {
		HashMap<String, Object> map = new HashMap<>();
		try {
			PreparedStatement pstmt = connection.prepareStatement("select * from `" + name + "` where `" + column + "` = " + parseToString(columnValue));
			ResultSet resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				for (String _column : valueColumn) {
					map.put(_column, resultSet.getObject(_column));
				}
				break;
			}
			resultSet.close();
			pstmt.close();
		}
		catch (Exception e) {
			print("数据库命令执行出错");
			print("错误原因: " + e.getMessage());
			if (e.getMessage().contains("closed")) {
				connect();
			}
		}
		return map;
	}
	
	public List<Object> getValues(String name, String column, int size) {
		return getValues(name, column, size, false);
	}
	
	public List<Object> getValues(String name, String column, int size, boolean desc) {
		List<Object> list = new ArrayList<>();
		try {
			PreparedStatement pstmt = connection.prepareStatement("select * from `" + name + "` order by `" + column + (size == -1 ? "`" + (desc ? " desc" : "") : (desc ? "desc " : "") + "` limit " + size));
			ResultSet resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				list.add(resultSet.getObject(column));
			}
			resultSet.close();
			pstmt.close();
		}
		catch (Exception e) {
			print("数据库命令执行出错");
			print("错误原因: " + e.getMessage());
			if (e.getMessage().contains("closed")) {
				connect();
			}
		}
		return list;
	}
	
	public LinkedList<HashMap<String, Object>> getValues(String name, String sortColumn, int size, String... valueColumn) {
		return getValues(name, sortColumn, size, false, valueColumn);
	}
	
	public LinkedList<HashMap<String, Object>> getValues(String name, String sortColumn, int size, boolean desc, String... valueColumn) {
		LinkedList<HashMap<String, Object>> list = new LinkedList<>();
		try {
			PreparedStatement pstmt = connection.prepareStatement("select * from `" + name + "` order by `" + sortColumn + (size == -1 ? "`" + (desc ? " desc" : "") : (desc ? "desc " : "") + "` limit " + size));
			ResultSet resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				HashMap<String, Object> map = new HashMap<>();
				for (String _column : valueColumn) {
					map.put(_column, resultSet.getObject(_column));
				}
				list.add(map);
			}
			resultSet.close();
			pstmt.close();
		}
		catch (Exception e) {
			print("数据库命令执行出错");
			print("错误原因: " + e.getMessage());
			if (e.getMessage().contains("closed")) {
				connect();
			}
		}
		return list;
	}
	
	public boolean execute(String sql) {
		try {
			// select * from user where userName = ? and password = ?
			PreparedStatement pstmt = connection.prepareStatement(sql);
			pstmt.execute();
			pstmt.close();
			return true;
		}
		catch (Exception e) {
			print("数据库命令执行出错");
			print("错误原因: " + e.getMessage());
			print("错误命令: " + sql);
			if (e.getMessage().contains("closed")) {
				connect();
			}
			return false;
		}
	}
	
	public boolean connect() {
		try {
			print("正在连接数据库");
			print("地址: " + connectionUrl);
			long time = System.currentTimeMillis();
			connection = DriverManager.getConnection(connectionUrl, this.user, this.password);
			print("数据库连接成功 (" + (System.currentTimeMillis() - time) + "ms)");
			return true;
		} 
		catch (SQLException e) {
			print("数据库连接失败");
			print("错误原因: " + e.getMessage());
			print("错误代码: " + e.getErrorCode());
			return false;
		} 
	}
	
	public boolean connect_SQLite() {
		try {
			print("正在连接数据库");
			print("地址: " + this.connectionUrl);
			long time = System.currentTimeMillis();
			connection = DriverManager.getConnection(connectionUrl);
			print("数据库连接成功 (" + (System.currentTimeMillis() - time) + "ms)");
			return true;
		} 
		catch (SQLException e) {
			print("数据库连接失败");
			print("错误原因: " + e.getMessage());
			print("错误代码: " + e.getErrorCode());
			return false;
		} 
	}
	
	public Connection getConnection() {
		return this.connection;
	}
	
	public String parseToString(Object object) {
		return object instanceof String ? "'" + object + "'" : object.toString();
	}
 	
	public void print(String message) {
		System.out.println("[TabooLib - MySQL] " + message);
	}
	
	private boolean loadDriverMySQL() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			return true;
		}
		catch (ClassNotFoundException e) {
			return false;
		}
	}
	
	private boolean loadDriverSQLite() {
		try {
			Class.forName("org.sqlite.JDBC");
			return true;
		}
		catch (ClassNotFoundException e) {
			return false;
		}
	}
	
	public static enum ColumnInteger {
		
		TINYINT, SMALLINT, MEDIUMINT, INT, BIGINT;
	}
	
	public static enum ColumnFloat {
		
		FLOAT, DOUBLE;
	}
	
	public static enum ColumnChar {
		
		CHAR, VARCHAR;
	}
	
	public static enum ColumnString {
		
		TINYTEXT, TEXT, MEDIUMTEXT, LONGTEXT;
	}
	
	public static class Column {
		
		private String name;
		private Object type;
		private int a;
		private int b;
		
		public Column(String name) {
			this.name = name;
			this.type = ColumnString.TEXT;
		}
		
		public Column(String name, ColumnInteger type) {
			this(name);
			this.type = type;
			this.a = 12;
		}
		
		public Column(String name, ColumnInteger type, int m) {
			this(name);
			this.type = type;
			this.a = m;
		}
		
		public Column(String name, ColumnFloat type, int m, int d) {
			this(name);
			this.type = type;
			this.a = m;
			this.b = d;
		}
		
		public Column(String name, ColumnChar type, int n) {
			this(name);
			this.type = type;
			this.a = n;
		}
		
		public Column(String name, ColumnString type) {
			this(name);
			this.type = type;
		}
		
		public String toString() {
			if (type instanceof ColumnInteger || type instanceof ColumnChar) {
				return "`" + name + "` " + type.toString().toLowerCase() + "(" + a + ")"; 
			}
			else if (type instanceof ColumnFloat) {
				return "`" + name + "` " + type.toString().toLowerCase() + "(" + a + "," + b + ")"; 
			}
			else {
				return "`" + name + "` " + type.toString().toLowerCase();
			}
		}
	}
}
