package me.skymc.taboolib.itagapi;

import com.google.common.base.Preconditions;
import me.skymc.taboolib.Main;
import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.common.function.TFunction;
import me.skymc.taboolib.packet.PacketUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.UUID;

/**
 * @Author sky
 * @Since 2018-05-23 0:37
 */
@TFunction(enable = "init")
public class TagDataHandler implements Listener {

    private static TagDataHandler handler;
    private HashMap<UUID, TagPlayerData> playersData = new HashMap<>();

    static void init() {
        Preconditions.checkArgument(handler == null, "TagDataHandler is already instanced!");
        handler = new TagDataHandler();
        // 注册监听
        Bukkit.getPluginManager().registerEvents(handler, TabooLib.instance());
        // 启动相关功能
        new BukkitRunnable() {

            @Override
            public void run() {
                if (PacketUtils.isProtocolLibEnabled()) {
                    TagPacket.inst();
                }
            }
        }.runTask(TabooLib.instance());
    }

    public TagPlayerData unregisterPlayerData(Player player) {
        return playersData.remove(player.getUniqueId());
    }

    public TagPlayerData getPlayerData(Player player) {
        return playersData.get(player.getUniqueId());
    }

    public TagPlayerData getPlayerDataComputeIfAbsent(Player player) {
        return playersData.computeIfAbsent(player.getUniqueId(), x -> new TagPlayerData(player));
    }

    public String getPrefix(Player player) {
        return getPlayerDataComputeIfAbsent(player).getPrefix();
    }

    public String getSuffix(Player player) {
        return getPlayerDataComputeIfAbsent(player).getSuffix();
    }

    public String getDisplay(Player player) {
        return getPlayerDataComputeIfAbsent(player).getNameDisplay();
    }

    public void setPrefix(Player player, String prefix) {
        updatePlayerVariable(getPlayerDataComputeIfAbsent(player).setPrefix(prefix));
        updatePlayerListName(player);
    }

    public void setSuffix(Player player, String suffix) {
        updatePlayerVariable(getPlayerDataComputeIfAbsent(player).setSuffix(suffix));
        updatePlayerListName(player);
    }

    public void setPrefixAndSuffix(Player player, String prefix, String suffix) {
        updatePlayerVariable(getPlayerDataComputeIfAbsent(player).setPrefix(prefix).setSuffix(suffix));
        updatePlayerListName(player);
    }

    public void setDisplay(Player player, String display) {
        TagPlayerData playerData = getPlayerDataComputeIfAbsent(player);
        cancelPlayerVariable(player, playerData);
        player.setDisplayName(playerData.setNameDisplay(display).getNameDisplay());
        updatePlayerVariable(playerData);
        updatePlayerListName(player);
        Bukkit.getScheduler().runTask(Main.getInst(), () -> TagPacket.refreshPlayer(player));
    }

    public void resetVariable(Player player) {
        updatePlayerVariable(getPlayerDataComputeIfAbsent(player).reset());
        updatePlayerListName(player);
    }

    public void resetDisplay(Player player) {
        setDisplay(player, player.getName());
    }

    public void reset(Player player) {
        updatePlayerVariable(getPlayerDataComputeIfAbsent(player).reset());
        setDisplay(player, player.getName());
    }

    // *********************************
    //
    //        Private Methods
    //
    // *********************************

    private void downloadPlayerVariable(Player player) {
        Scoreboard scoreboard = TagUtils.getScoreboardComputeIfAbsent(player);
        playersData.values().forEach(playerData -> updateTeamVariable(scoreboard, playerData));
    }

    private void updatePlayerVariable(TagPlayerData playerData) {
        Bukkit.getOnlinePlayers().forEach(online -> updateTeamVariable(TagUtils.getScoreboardComputeIfAbsent(online), playerData));
    }

    private void updatePlayerListName(Player player) {
        TagPlayerData playerData = getPlayerDataComputeIfAbsent(player);
        player.setPlayerListName(!playerData.getNameDisplay().equals(player.getName()) ? playerData.getPrefix() + playerData.getNameDisplay() + playerData.getSuffix() : playerData.getNameDisplay());
    }

    private void updateTeamVariable(Scoreboard scoreboard, TagPlayerData playerData) {
        Team entryTeam = TagUtils.getTeamComputeIfAbsent(scoreboard, playerData.getTeamHash());
        entryTeam.addEntry(playerData.getNameDisplay());
        entryTeam.setPrefix(playerData.getPrefix());
        entryTeam.setSuffix(playerData.getSuffix());
        // 傻逼 BedWarsRel 我草你妈的
        if (TabooLib.instance().getConfig().getBoolean("TABLIST-AUTO-CLEAN-TEAM", true)) {
            TagUtils.cleanEmptyTeamInScoreboard(scoreboard);
        }
    }

    private void cancelPlayerVariable(Player player, TagPlayerData playerData) {
        if (playerData == null) {
            return;
        }
        for (Player online : Bukkit.getOnlinePlayers()) {
            Scoreboard scoreboard = TagUtils.getScoreboardComputeIfAbsent(player);
            TagUtils.cleanEntryInScoreboard(scoreboard, playerData.getNameDisplay());
            // 傻逼 BedWarsRel 我草你妈的
            if (TabooLib.instance().getConfig().getBoolean("TABLIST-AUTO-CLEAN-TEAM", true)) {
                TagUtils.cleanEmptyTeamInScoreboard(scoreboard);
            }
        }
    }

    // *********************************
    //
    //            Listeners
    //
    // *********************************

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        downloadPlayerVariable(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        cancelPlayerVariable(e.getPlayer(), unregisterPlayerData(e.getPlayer()));
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        if (e.getMessage().equalsIgnoreCase("/scoreboardinfo") && e.getPlayer().hasPermission("itagapi.info")) {
            e.setCancelled(true);

            e.getPlayer().sendMessage("§7计分板信息:");
            Scoreboard scoreboard = TagUtils.getScoreboardComputeIfAbsent(e.getPlayer());

            for (Team team : scoreboard.getTeams()) {
                e.getPlayer().sendMessage("§7队伍: §f" + team.getName());
                e.getPlayer().sendMessage("§f - §7前缀: §f" + team.getPrefix());
                e.getPlayer().sendMessage("§f - §7后缀: §f" + team.getSuffix());
                e.getPlayer().sendMessage("§f - §7成员: §f");
                team.getEntries().forEach(entry -> e.getPlayer().sendMessage("§f    - §f" + entry));
            }
        }
    }

    // *********************************
    //
    //        Getter and Setter
    //
    // *********************************

    public static TagDataHandler getHandler() {
        return handler;
    }
}
