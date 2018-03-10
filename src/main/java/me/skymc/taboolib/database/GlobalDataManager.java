package me.skymc.taboolib.database;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import me.skymc.taboolib.Main;
import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.message.MsgUtils;
import me.skymc.taboolib.Main.StorageType;
import me.skymc.taboolib.playerdata.DataUtils;

public class GlobalDataManager {
	
	public static FileConfiguration data = DataUtils.addPluginData("TabooLibrary-Variable.yml", null);
	
	/**
	 * 获取变量
	 * 
	 * @param name 名称
	 * @param defaultVariable 默认值
	 * @return
	 */
	public static String getVariable(String name, String defaultVariable) {
		if (Main.getStorageType() == StorageType.SQL) {
			Object obj = Main.getConnection().getValueLast(Main.getTablePrefix() + "_plugindata", "name", name, "variable");
			return obj != null ? obj.toString().equals("null") ? defaultVariable : obj.toString() : defaultVariable;
		}
		else {
			return data.contains(name.replace(":", "-")) ? data.getString(name.replace(":", "-")) : defaultVariable;
		}
	}
	
	/**
	 * 获取缓存变量（该方法仅限数据库储存方式）
	 * 
	 * @param name 名称
	 * @param defaultVariable 默认值
	 * @return
	 */
	public static String getVariableAsynchronous(String name, String defaultVariable) {
		if (Main.getStorageType() == StorageType.SQL) {
			SQLVariable variable = SQLMethod.getSQLVariable(name);
			return variable == null ? defaultVariable : variable.getVariable().equals("null") ? defaultVariable : variable.getVariable();
		}
		else {
			return getVariable(name, defaultVariable);
		}
	}
	
	/**
	 * 设置变量
	 * 
	 * @param name 名称
	 * @param variable 变量
	 */
	public static void setVariable(String name, String variable) {
		if (Main.getStorageType() == StorageType.SQL) {
			Main.getConnection().intoValue(Main.getTablePrefix() + "_plugindata", name, variable == null ? "null" : variable, TabooLib.getServerUID());
		}
		else {
			data.set(name.replace(":", "-"), variable);
		}
	}
	
	/**
	 * 设置缓存变量（该方法仅限数据库储存方式）
	 * 
	 * @param name
	 * @param variable
	 */
	public static void setVariableAsynchronous(String name, String variable) {
		if (Main.getStorageType() == StorageType.SQL) {
			SQLVariable _variable = SQLMethod.contains(name) ? SQLMethod.getSQLVariable(name).setVariable(variable == null ? "null" : variable) : SQLMethod.addSQLVariable(name, variable == null ? "null" : variable);
			// 更新数据
			SQLMethod.uploadVariable(_variable, true);
		}
		else {
			setVariable(name, variable);
		}
	}
	
	/**
	 * 检查变量是否存在
	 * 
	 * @param name 名称
	 */
	public static boolean contains(String name) {
		if (Main.getStorageType() == StorageType.SQL) {
			return getVariable(name, null) == null ? false : true;
		}
		else {
			return data.contains(name.replace(":", "-"));
		}
	}
	
	/**
	 * 检查变量是否被缓存（该方法仅限数据库储存方式）
	 * 
	 * @param name 名称
	 * @return
	 */
	public static boolean containsAsynchronous(String name) {
		if (Main.getStorageType() == StorageType.SQL) {
			return getVariableAsynchronous(name, null) == null ? false : true;
		}
		else {
			return contains(name);
		}
	}
	
	/**
	 * 清理所有失效的变量
	 * 该方法仅限数据库储存时有效
	 */
	public static void clearInvalidVariables() {
		if (Main.getStorageType() == StorageType.SQL) {
			HashMap<String, String> map = getVariables();
			Main.getConnection().truncateTable(Main.getTablePrefix() + "_plugindata");
			for (String name : map.keySet()) {
				Main.getConnection().intoValue(Main.getTablePrefix() + "_plugindata", name, map.get(name), TabooLib.getServerUID());
			}
		}
	}
	
	/**
	 * 获取所有有效变量
	 * 
	 * @return
	 */
	public static HashMap<String, String> getVariables() {
		HashMap<String, String> map = new HashMap<>();
		if (Main.getStorageType() == StorageType.SQL) {
			LinkedList<HashMap<String, Object>> list = Main.getConnection().getValues(Main.getTablePrefix() + "_plugindata", "id", -1, false, "name", "variable");
			for (HashMap<String, Object> _map : list) {
				if (!_map.get("variable").toString().equals("null")) {
					map.put(_map.get("name").toString(), _map.get("variable").toString());
				}
			}
		}
		else {
			for (String name : data.getConfigurationSection("").getKeys(false)) {
				map.put(name, data.getString(name));
			}
		}
		return map;
	}
	
	/**
	 * 获取缓存变量（该方法仅限数据库储存方式）
	 * 
	 * @return
	 */
	public static HashMap<String, String> getVariablesAsynchronous() {
		if (Main.getStorageType() == StorageType.SQL) {
			HashMap<String, String> map = new HashMap<>();
			for (SQLVariable variable : SQLMethod.getSQLVariables()) {
				if (!variable.getVariable().equals("null")) {
					map.put(variable.getName(), variable.getVariable());
				}
			}
			return map;
		}
		else {
			return getVariables();
		}
	}
	
	/**
	 * 数据库变量
	 * 
	 * @author sky
	 *
	 */
	public static class SQLVariable {
		
		public String name = "";
		public String variable = "";
		public String upgradeUID = "";
		
		public SQLVariable(String name, String variable, String upgradeUID) {
			this.name = name;
			this.variable = variable;
			this.upgradeUID = upgradeUID;
		}
		
		public String getName() {
			return name;
		}
		
		public String getVariable() {
			return variable;
		}
		
		public SQLVariable setVariable(String args) {
			this.variable = args;
			return this;
		}
		
		public String getUpgradeUID() {
			return upgradeUID;
		}
	}
	
	/**
	 * 数据库方法
	 * 
	 * @author sky
	 *
	 */
	public static class SQLMethod {
		
		private static ConcurrentHashMap<String, SQLVariable> variables = new ConcurrentHashMap<>();
		
		/**
		 * 获取数据
		 * 
		 * @param name 名字
		 */
		public static SQLVariable getSQLVariable(String name) {
			return variables.get(name);
		}
		
		/**
		 * 获取所有变量
		 * 
		 * @return
		 */
		public static Collection<SQLVariable> getSQLVariables() {
			return variables.values();
		}
		
		/**
		 * 添加一个变量
		 * 
		 * @param name 名字
		 * @param value 值
		 * @return
		 */
		public static SQLVariable addSQLVariable(String name, String value) {
			SQLVariable variable = new SQLVariable(name, value, TabooLib.getServerUID());
			variables.put(name, variable);
			return variable;
		}
		
		/**
		 * 移除一个变量
		 * 
		 * @param name 名字
		 * @return
		 */
		public static SQLVariable removeSQLVariable(String name) {
			if (variables.contains(name)) {
				variables.get(name).setVariable("null");
			}
			return variables.get(name);
		}
		
		/**
		 * 是否包含变量
		 * 
		 * @param name 名字
		 * @return
		 */
		public static boolean contains(String name) {
			return variables.containsKey(name);
		}
		
		/**
		 * 载入数据库中的所有变量缓存
		 * 
		 * @param sync 是否异步
		 */
		public static void loadVariables(boolean sync) {
			if (Main.getStorageType() == StorageType.LOCAL) {
				return;
			}
			
			BukkitRunnable runnable = new BukkitRunnable() {
				
				@Override
				public void run() {
					LinkedList<HashMap<String, Object>> list = Main.getConnection().getValues(Main.getTablePrefix() + "_plugindata", "id", -1, false, "name", "variable", "upgrade");
					for (HashMap<String, Object> _map : list) {
						if (!_map.get("variable").toString().equals("null")) {
							variables.put(_map.get("name").toString(), new SQLVariable(_map.get("name").toString(), _map.get("variable").toString(), _map.get("upgrade").toString()));
						}
					}
				}
			};
			
			if (sync) {
				runnable.runTaskAsynchronously(Main.getInst());
			}
			else {
				runnable.run();
			}
		}
		
		/**
		 * 检查当前变量是否被其他服务器更新
		 * 
		 * @param sync 是否异步
		 */
		public static void checkVariable(boolean sync) {
			if (Main.getStorageType() == StorageType.LOCAL) {
				return;
			}
			
			BukkitRunnable runnable = new BukkitRunnable() {
				
				@Override
				public void run() {
					/**
					 * 根据正序排列获取所有变量
					 * 新的变量会覆盖旧的变量
					 */
					LinkedList<HashMap<String, Object>> list = Main.getConnection().getValues(Main.getTablePrefix() + "_plugindata", "id", -1, false, "name", "variable", "upgrade");
					// 循环变量
					for (HashMap<String, Object> value : list) {
						Object name = value.get("name");
						try {
							// 如果变量存在
							if (variables.containsKey(name)) {
								// 如果变量不是由本服更新
								if (!value.get("upgrade").equals(variables.get(name).getUpgradeUID())) {
									// 如果变量是空
									if (value.get("variable").equals("null")) {
										// 删除变量
										variables.remove(name);
									}
									else {
										// 更新变量
										variables.get(name).setVariable(value.get("variable").toString());
									}
								}
							}
							// 如果变量存在则下载到本地
							else if (!value.get("variable").equals("null")) {
								variables.put(value.get("name").toString(), new SQLVariable(value.get("name").toString(), value.get("variable").toString(), value.get("upgrade").toString()));
							}
						}
						catch (Exception e) {
							// 移除
							variables.remove(name);
							// 提示
							MsgUtils.warn("变量出现异常: &4" + name);
							MsgUtils.warn("原因: &4" + e.getMessage());
						}
					}
				}
			};
			
			if (sync) {
				runnable.runTaskAsynchronously(Main.getInst());
			}
			else {
				runnable.run();
			}
		}
		
		/**
		 * 向数据库上传所有数据
		 * 
		 * @param sync 是否异步
		 */
		public static void uploadVariables(boolean sync) {
			if (Main.getStorageType() == StorageType.LOCAL) {
				return;
			}
			
			for (SQLVariable variable : variables.values()) {
				uploadVariable(variable, sync);
			}
		}
		
		/**
		 * 向数据库上传当前数据
		 * 
		 * @param variable 数据
		 * @param sync 是否异步
		 */
		public static void uploadVariable(SQLVariable variable, boolean sync) {
			if (Main.getStorageType() == StorageType.LOCAL) {
				return;
			}
			
			BukkitRunnable runnable = new BukkitRunnable() {
				
				@Override
				public void run() {
					Main.getConnection().intoValue(Main.getTablePrefix() + "_plugindata", variable.getName(), variable.getVariable() == null ? "null" : variable.getVariable(), TabooLib.getServerUID());
				}
			};
			
			if (sync) {
				runnable.runTaskAsynchronously(Main.getInst());
			}
			else {
				runnable.run();
			}
		}
		
		/**
		 * 启动数据库储存方法
		 * 
		 */
		public static void startSQLMethod() {
			long time = System.currentTimeMillis();
			// 载入数据
			loadVariables(false);
			// 提示信息
			MsgUtils.send("从数据库中获取 &f" + variables.size() + " &7个变量, 耗时: &f" + (System.currentTimeMillis() - time) + " &7(ms)");
			
			// 检查更新
			new BukkitRunnable() {
				
				@Override
				public void run() {
					checkVariable(true);
				}
			}.runTaskTimerAsynchronously(Main.getInst(), Main.getInst().getConfig().getInt("PluginData.CHECK-DELAY") * 20, Main.getInst().getConfig().getInt("PluginData.CHECK-DELAY") * 20);
		}
		
		/**
		 * 结束数据库储存方法
		 * 
		 */
		public static void cancelSQLMethod() {
			// 上传数据
			uploadVariables(false);
		}
	}
}
