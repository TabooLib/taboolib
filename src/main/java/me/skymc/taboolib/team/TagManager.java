package me.skymc.taboolib.team;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import lombok.Getter;
import lombok.Setter;
import me.skymc.taboolib.Main;

/**
 * @author sky
 * @since 2018-03-17 21:43:49
 */
public class TagManager implements Listener {
	
	private static TagManager inst;
	
	@Getter
	private HashMap<String, PlayerData> playerdata = new HashMap<>();
	
	private TagManager() {
		Bukkit.getPluginManager().registerEvents(this, Main.getInst());
	}
	
	public static TagManager getInst() {
		synchronized (TagManager.class) {
			if (inst == null) {
				inst = new TagManager();
			}
		}
		return inst;
	}
	
	/**
	 * 设置玩家前缀
	 * 
	 * @param player 名称
	 * @param prefix 前缀
	 */
	public void setPrefix(Player player, String prefix) {
		getPlayerData(player).setPrefix(prefix);
		uploadData(player);
	}
	
	/**
	 * 设置玩家后缀
	 * 
	 * @param player 玩家
	 * @param suffix 后缀
	 */
	public void setSuffix(Player player, String suffix) {
		getPlayerData(player).setSuffix(suffix);
		uploadData(player);
	}
	
	/**
	 * 获取玩家前缀
	 * 
	 * @param player 玩家
	 * @return String
	 */
	public String getPrefix(Player player) {
		return getPlayerData(player).getPrefix();
	}
	
	/**
	 * 获取玩家后缀
	 * 
	 * @param player 玩家
	 * @return String
	 */
	public String getSuffix(Player player) {
		return getPlayerData(player).getSuffix();
	}
	
	/**
	 * 获取玩家数据
	 * 
	 * @param player 玩家
	 * @return {@link PlayerData}
	 */
	private PlayerData getPlayerData(Player player) {
		PlayerData data = playerdata.get(player.getName());
		if (data == null) {
			data = new PlayerData(player.getName());
			playerdata.put(player.getName(), data);
		}
		return data;
	}
	
	/**
	 * 删除该玩家的称号数据
	 * 
	 * @param player
	 */
	public void removeData(Player player) {
		playerdata.remove(player.getName());
		for (Player _player : Bukkit.getOnlinePlayers()) {
			Scoreboard scoreboard = _player.getScoreboard();
			if (scoreboard != null) {
				Team team = scoreboard.getTeam(player.getName());
				if (team != null) {
					team.unregister();
				}
			}
		}
	}
	
	/**
	 * 将该玩家的数据向服务器所有玩家更新
	 * 
	 * @param player 玩家
	 */
	public void uploadData(Player player) {
		PlayerData data = getPlayerData(player);
		String prefix = data.getPrefix().length() > 16 ? data.getPrefix().substring(0, 16) : data.getPrefix();
		String suffix = data.getSuffix().length() > 16 ? data.getSuffix().substring(0, 16) : data.getSuffix();
		// 如果没有称号数据
		if (prefix.isEmpty() && suffix.isEmpty()) {
			return;
		}
		
		for (Player _player : Bukkit.getOnlinePlayers()) {
			Scoreboard scoreboard = _player.getScoreboard();
			if (scoreboard == null) {
				_player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
			}
			Team team = scoreboard.getTeam(player.getName());
			if (team == null) {
				team = scoreboard.registerNewTeam(player.getName());
				team.addEntry(player.getName());
			}
			team.setPrefix(prefix);
			team.setSuffix(suffix);
		}
	}
	
	/**
	 * 下载服务器内的称号数据到该玩家
	 * 
	 * @param player 玩家
	 */
	public void downloadData(Player player) {
		Scoreboard scoreboard = player.getScoreboard();
		if (scoreboard == null) {
			player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		}
		
		for (Player _player : Bukkit.getOnlinePlayers()) {
			PlayerData data = getPlayerData(_player);
			String prefix = data.getPrefix().length() > 16 ? data.getPrefix().substring(0, 16) : data.getPrefix();
			String suffix = data.getSuffix().length() > 16 ? data.getSuffix().substring(0, 16) : data.getSuffix();
			// 如果没有称号数据
			if (prefix.isEmpty() && suffix.isEmpty()) {
				continue;
			}
			
			Team team = scoreboard.getTeam(_player.getName());
			if (team == null) {
				team = scoreboard.registerNewTeam(_player.getName());
				team.addEntry(_player.getName());
			}
			team.setPrefix(prefix);
			team.setSuffix(suffix);
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		downloadData(e.getPlayer());
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		removeData(e.getPlayer());
	}
	
	static class PlayerData {
		
		@Getter
		private String name;
		
		@Getter
		@Setter
		private String prefix;
		
		@Getter
		@Setter
		private String suffix;
		
		public PlayerData(String name) {
			this.name = name;
			this.prefix = "";
			this.suffix = "";
		}
	}
}
