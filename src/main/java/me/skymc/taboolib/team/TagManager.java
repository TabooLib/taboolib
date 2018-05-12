package me.skymc.taboolib.team;

import com.ilummc.tlib.util.Strings;
import me.skymc.taboolib.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.UUID;

/**
 * @author sky
 * @since 2018-03-17 21:43:49
 */
public class TagManager implements Listener {

    private static TagManager inst;

    private HashMap<UUID, PlayerData> playerData = new HashMap<>();

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

    public HashMap<UUID, PlayerData> getPlayerData() {
        return playerData;
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
    public PlayerData getPlayerData(Player player) {
        return playerData.computeIfAbsent(player.getUniqueId(), k -> new PlayerData(player));
    }

    /**
     * 注销称号数据
     *
     * @param targetPlayer
     */
    public void unloadData(Player targetPlayer) {
        PlayerData data = getPlayerData(targetPlayer);
        for (Player player : Bukkit.getOnlinePlayers()) {
            Scoreboard scoreboard = getScoreboard(player);
            Team team = scoreboard.getTeam(data.getName());
            if (team != null) {
                team.unregister();
            }
        }
        data.reset();
    }

    /**
     * 将该玩家的数据向服务器所有玩家更新
     *
     * @param targetPlayer 玩家
     */
    public void uploadData(Player targetPlayer) {
        PlayerData data = getPlayerData(targetPlayer);
        String prefix = data.getPrefix().length() > 16 ? data.getPrefix().substring(0, 16) : data.getPrefix();
        String suffix = data.getSuffix().length() > 16 ? data.getSuffix().substring(0, 16) : data.getSuffix();
        // 如果没有称号数据
        if (prefix.isEmpty() && suffix.isEmpty()) {
            unloadData(targetPlayer);
            return;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            Scoreboard scoreboard = getScoreboard(player);
            Team team = getTeam(scoreboard, data);
            team.setPrefix(prefix);
            team.setSuffix(suffix);
        }
    }

    /**
     * 下载服务器内的称号数据到该玩家
     *
     * @param targetPlayer 玩家
     */
    public void downloadData(Player targetPlayer) {
        Scoreboard scoreboard = getScoreboard(targetPlayer);
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData data = getPlayerData(player);
            String prefix = data.getPrefix().length() > 16 ? data.getPrefix().substring(0, 16) : data.getPrefix();
            String suffix = data.getSuffix().length() > 16 ? data.getSuffix().substring(0, 16) : data.getSuffix();
            // 如果没有称号数据
            if (prefix.isEmpty() && suffix.isEmpty()) {
                continue;
            }
            Team team = getTeam(scoreboard, data);
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
        unloadData(e.getPlayer());
    }

    private Scoreboard getScoreboard(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        if (scoreboard == null) {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
        return scoreboard;
    }

    private Team getTeam(Scoreboard scoreboard, PlayerData data) {
        Team team = scoreboard.getTeam(data.getName());
        if (team == null) {
            team = scoreboard.registerNewTeam(data.getName());
            team.addEntry(data.getName());
        }
        return team;
    }

    static class PlayerData {

        private UUID uuid;
        private String name;
        private String prefix;
        private String suffix;

        public PlayerData(Player player) {
            this.uuid = player.getUniqueId();
            this.name = player.getName();
            this.prefix = "";
            this.suffix = "";
        }

        public UUID getUuid() {
            return uuid;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public String getSuffix() {
            return suffix;
        }

        public void setSuffix(String suffix) {
            this.suffix = suffix;
        }

        public boolean isEmpty() {
            return Strings.isEmpty(suffix) && Strings.isEmpty(prefix);
        }

        public void reset() {
            prefix = "";
            suffix = "";
        }
    }
}
