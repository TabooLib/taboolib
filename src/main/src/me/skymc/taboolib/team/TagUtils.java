package me.skymc.taboolib.team;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import me.skymc.taboolib.message.MsgUtils;
import me.skymc.taboolib.methods.MethodsUtils;

@Deprecated
public class TagUtils {
	
	public static void setTag(Player p, String prefix, String suffix) throws Exception {
		if (suffix.length() > 16) {
			suffix = suffix.substring(0, 16);
		}
		Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
		Team t = board.getTeam(p.getName());
		if (t == null)
		{
			t = board.registerNewTeam(p.getName());
			t.setPrefix(prefix);
			t.setSuffix(suffix);
			t.addPlayer(p);
			
		}
		else
		{
			t = board.getTeam(p.getName());
			t.setPrefix(prefix);
			t.setSuffix(suffix);
			t.addPlayer(p);
		}
		for (Player o : Bukkit.getOnlinePlayers()) {
			o.setScoreboard(board);
		}
	}
  
	public static void unregisterTag(Player p) throws Exception 
	{
		Bukkit.getScoreboardManager().getMainScoreboard().getPlayerTeam(p).unregister();
	}
	
	public static void delete() {
		try {
			for (Team t : Bukkit.getScoreboardManager().getMainScoreboard().getTeams()) {
				t.unregister();
			}
		}
		catch (Exception e) {
			// TODO: handle exception
		}
	}
  
	public static void unregisterAll() throws Exception
	{
		for (Player o : Bukkit.getOnlinePlayers()) {
			unregisterTag(o);
		}
	}
  
	public static void registerAll(String prefix, String suffix) throws Exception
	{
		for (Player o : Bukkit.getOnlinePlayers()) {
			setTag(o, prefix, " " + suffix);
		}
	}
  
	public static void refresh()
	{
		Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
		for (Player o : Bukkit.getOnlinePlayers()) {
			o.setScoreboard(board);
		}
	}
  
	public static String getPrefix(Player p) throws Exception
	{
		Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
		Team t = board.getTeam(p.getName());
		if ((t != null) && (board.getPlayerTeam(p).getPrefix() != null) && (!board.getPlayerTeam(p).getPrefix().isEmpty())) {
			return board.getPlayerTeam(p).getPrefix();
		}
		return "";
	}
  
	public static String getSuffix(Player p) throws Exception
	{
		Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
		Team t = board.getTeam(p.getName());
		if ((t != null) && (board.getPlayerTeam(p).getSuffix() != null) && (!board.getPlayerTeam(p).getSuffix().isEmpty())) {
			return board.getPlayerTeam(p).getSuffix();
		}
		return "";
	}
}
