package me.skymc.taboolib.team;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import me.skymc.taboolib.message.MsgUtils;
import me.skymc.taboolib.methods.MethodsUtils;

/**
 * @author sky
 * @since 2018-03-17 21:36:52
 */
public class TagUtils {
	
	/**
	 * 设置玩家前后缀
	 * 
	 * @param p 玩家
	 * @param prefix 前缀
	 * @param suffix 后缀
	 */
	public static void setTag(Player p, String prefix, String suffix) {
		// 判断长度
		if (prefix.length() > 16) {
			prefix = prefix.substring(0, 16);
		} 
		if (suffix.length() > 16) {
			suffix = suffix.substring(0, 16);
		}
		
		// 获取计分板
		Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
		Team t = board.getTeam(p.getName());
		if (t == null) {
			t = board.registerNewTeam(p.getName());
		}
		
		// 更新称号
		t.setPrefix(prefix);
		t.setSuffix(suffix);
		t.addEntry(p.getName());
		
		// 更新玩家
		for (Player o : Bukkit.getOnlinePlayers()) {
			if (!board.equals(o.getScoreboard())) {
				o.setScoreboard(board);
			}
		}
	}
  
	/**
	 * 注销玩家前后缀
	 * 
	 * @param p 玩家
	 */
	public static void unregisterTag(Player p) {
		Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(p.getName());
		if (team != null) {
			team.unregister();
		}
	}
	
	/**
	 * 注销所有在线玩家前后缀
	 */
	public static void unregisterAll() {
		for (Player o : Bukkit.getOnlinePlayers()) {
			unregisterTag(o);
		}
	}
	
	/**
	 * 删除所有前后缀
	 */
	public static void delete() {
		for (Team t : Bukkit.getScoreboardManager().getMainScoreboard().getTeams()) {
			t.unregister();
		}
	}
  
	/**
	 * 设置全服玩家前后缀
	 * 
	 * @param prefix 前缀
	 * @param suffix 后缀
	 */
	public static void registerAll(String prefix, String suffix) {
		for (Player o : Bukkit.getOnlinePlayers()) {
			setTag(o, prefix, suffix);
		}
	}
  
	/**
	 * 刷新计分板数据
	 */
	public static void refresh() {
		Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
		for (Player o : Bukkit.getOnlinePlayers()) {
			o.setScoreboard(board);
		}
	}
  
	/**
	 * 获取玩家前缀
	 * 
	 * @param p 玩家
	 * @return String
	 */
	public static String getPrefix(Player p) {
		Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
		Team t = board.getTeam(p.getName());
		if (t != null) {
			return t.getPrefix();
		}
		return null;
	}
  
	/**
	 * 获取玩玩家后缀
	 * 
	 * @param p 玩家
	 * @return String
	 */
	public static String getSuffix(Player p) {
		Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
		Team t = board.getTeam(p.getName());
		if (t != null) {
			return t.getSuffix();
		}
		return null;
	}
}
