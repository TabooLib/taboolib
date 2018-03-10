package me.skymc.taboolib.database;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import me.skymc.taboolib.Main;
import me.skymc.taboolib.Main.StorageType;
import me.skymc.taboolib.events.PlayerLoadedEvent;
import me.skymc.taboolib.exception.PlayerOfflineException;
import me.skymc.taboolib.fileutils.ConfigUtils;
import me.skymc.taboolib.fileutils.FileUtils;
import me.skymc.taboolib.message.MsgUtils;

public class PlayerDataManager implements Listener {
	
	private static final ConcurrentHashMap<String, FileConfiguration> PLAYER_DATA = new ConcurrentHashMap<>();
	
	public static enum UsernameType {
		UUID, USERNAME;
	}
	
	/**
	 * 获取用户储存方式
	 * 
	 * @return
	 */
	public static UsernameType getUsernameType() {
		return Main.getInst().getConfig().getBoolean("ENABLE-UUID") ? UsernameType.UUID : UsernameType.USERNAME;
	}
	
	/**
	 * 获取玩家数据
	 * 
	 * @param player 玩家
	 * @return
	 * @throws PlayerOfflineException 
	 */
	public static FileConfiguration getPlayerData(Player player) {
		if (getUsernameType() == UsernameType.UUID) {
			return getPlayerData(player.getUniqueId().toString(), false);
		}
		else {
			return getPlayerData(player.getName(), false);
		}
	}
	
	/**
	 * 获取玩家数据
	 * 
	 * @param player
	 * @return
	 */
	public static FileConfiguration getPlayerData(OfflinePlayer player) {
		if (!player.isOnline()) {
			return null;
		}
		if (getUsernameType() == UsernameType.UUID) {
			return getPlayerData(player.getUniqueId().toString(), false);
		}
		else {
			return getPlayerData(player.getName(), false);
		}
	}
	
	/**
	 * 读取玩家数据
	 * 
	 * @param username 玩家
	 * @return
	 * @throws PlayerOfflineException 
	 */
	public static FileConfiguration getPlayerData(String username, boolean offline) {
		if (PLAYER_DATA.containsKey(username)) {
			return PLAYER_DATA.get(username);
		}
		else if (offline) {
			if (Main.getStorageType() == StorageType.SQL) {
				throw new PlayerOfflineException("不允许在储存模式为数据库的情况下获取离线玩家数据");
			}
			return loadPlayerData(username);
		}
		return null;
	}
	
	/**
	 * 载入玩家数据
	 * 
	 * @param username 玩家
	 * @return
	 */
	public static FileConfiguration loadPlayerData(String username) {
		// 本地储存
		if (Main.getStorageType() == StorageType.LOCAL) {
			// 读取文件
			File file = FileUtils.file(Main.getPlayerDataFolder(), username + ".yml");
			// 载入配置
			PLAYER_DATA.put(username, YamlConfiguration.loadConfiguration(file));
		}
		else {
			// 数据是否存在
			if (Main.getConnection().isExists(Main.getTablePrefix() + "_playerdata", "username", username)) {
				// 获取数据
				String code = Main.getConnection().getValue(Main.getTablePrefix() + "_playerdata", "username", username, "configuration").toString();
				try {
					// 载入配置
					PLAYER_DATA.put(username, ConfigUtils.decodeYAML(code));
				}
				catch (Exception e) {
					// 创建空数据
					PLAYER_DATA.put(username, new YamlConfiguration());
					// 反馈信息
					MsgUtils.warn("玩家 &4" + username + " &c的数据载入出现异常: &4" + e.getMessage());
				}
			}
			else {
				// 创建空数据
				PLAYER_DATA.put(username, new YamlConfiguration());
			}
		}
		return PLAYER_DATA.get(username);
	}
	
	/**
	 * 保存玩家数据
	 * 
	 * @param username 玩家
	 * @param remove 是否移除缓存
	 */
	public static void savePlayerData(String username, boolean remove) {
		// 没有数据
		if (!PLAYER_DATA.containsKey(username)) {
			return;
		}
		// 本地储存
		if (Main.getStorageType() == StorageType.LOCAL) {
			// 读取文件
			File file = FileUtils.file(Main.getPlayerDataFolder(), username + ".yml");
			// 保存配置
			try {
				PLAYER_DATA.get(username).save(file);
			}
			catch (Exception e) {
				// TODO: handle exception
			}
		}
		// 如果是数据库储存且有数据
		else if (PLAYER_DATA.get(username).getConfigurationSection("").getKeys(false).size() > 0) {
			// 数据是否存在
			if (Main.getConnection().isExists(Main.getTablePrefix() + "_playerdata", "username", username)) {
				// 写入数据
				Main.getConnection().setValue(Main.getTablePrefix() + "_playerdata", "username", username, "configuration", ConfigUtils.encodeYAML(PLAYER_DATA.get(username)));
			}
			else {
				// 插入数据
				Main.getConnection().intoValue(Main.getTablePrefix() + "_playerdata", username, ConfigUtils.encodeYAML(PLAYER_DATA.get(username)));
			}
		}
		// 获取这个属性对应的玩家
		Player player;
		if (getUsernameType() == UsernameType.UUID) {
			player = Bukkit.getPlayer(UUID.fromString(username));
		}
		else {
			player = Bukkit.getPlayerExact(username);
		}
		// 如果移除数据 或 玩家不在线
		if (remove || player == null) {
			PLAYER_DATA.remove(username);
		}
	}
	
	/**
	 * 保存所有玩家的缓存
	 * 
	 * @param sync 是否异步进行
	 * @param remove 是否移除数据
	 */
	public static void saveAllCaches(boolean sync, boolean remove) {
		BukkitRunnable runnable = new BukkitRunnable() {
			
			@Override
			public void run() {
				long time = System.currentTimeMillis();
				// 保存
				for (String name : PLAYER_DATA.keySet()) {
					savePlayerData(name, false);
				}
				// 提示
				if (!Main.getInst().getConfig().getBoolean("HIDE-NOTIFY")) {
					MsgUtils.send("保存 &f" + PLAYER_DATA.size() + " &7条玩家数据, 耗时: &f" + (System.currentTimeMillis() - time) + " &7(ms)");
				}
			}
		};
		// 如果异步
		if (sync) {
			runnable.runTaskAsynchronously(Main.getInst());
		}
		// 如果同步
		else {
			runnable.run();
		}
	}
	
	/**
	 * 保存所有玩家的数据
	 * 
	 * @param sync 是否异步进行
	 * @param remove 是否移除数据
	 */
	public static void saveAllPlayers(boolean sync, boolean remove) {
		// 创建任务
		BukkitRunnable runnable = new BukkitRunnable() {
			
			@Override
			public void run() {
				for (Player player : Bukkit.getOnlinePlayers()) {
					savePlayerData(Main.getInst().getConfig().getBoolean("ENABLE-UUID") ? player.getUniqueId().toString() : player.getName(), remove);
				}
			}
		};
		// 如果异步
		if (sync) {
			runnable.runTaskAsynchronously(Main.getInst());
		}
		// 如果同步
		else {
			runnable.run();
		}
	}
	
	@EventHandler
	public void join(PlayerJoinEvent e) {
		new BukkitRunnable() {
			
			@Override
			public void run() {
				// 载入数据
				loadPlayerData(Main.getInst().getConfig().getBoolean("ENABLE-UUID") ? e.getPlayer().getUniqueId().toString() : e.getPlayer().getName());
				// 载入完成
				Bukkit.getPluginManager().callEvent(new PlayerLoadedEvent(e.getPlayer()));
			}
		}.runTaskAsynchronously(Main.getInst());
	}
	
	@EventHandler
	public void quit(PlayerQuitEvent e) {
		if (!Main.isDisable()) {
			new BukkitRunnable() {
				
				@Override
				public void run() {
					// 保存数据
					savePlayerData(Main.getInst().getConfig().getBoolean("ENABLE-UUID") ? e.getPlayer().getUniqueId().toString() : e.getPlayer().getName(), true);
				}
			}.runTaskAsynchronously(Main.getInst());
		}
	}
}
