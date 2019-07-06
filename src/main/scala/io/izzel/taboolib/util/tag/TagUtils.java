package io.izzel.taboolib.util.tag;

import io.izzel.taboolib.module.locale.logger.TLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Optional;

/**
 * @Author sky
 * @Since 2018-05-21 15:09
 */
public class TagUtils {

    public static void cleanTeamInScoreboard(Scoreboard scoreboard) {
        try {
            scoreboard.getTeams().forEach(Team::unregister);
        } catch (Exception e) {
            TLogger.getGlobalLogger().error("TagUtils.cleanTeamInScoreboard() 异常: " + e.toString());
        }
    }

    public static void cleanEntryInScoreboard(Scoreboard scoreboard, String entry) {
        try {
            Optional.ofNullable(scoreboard.getEntryTeam(entry)).ifPresent(x -> x.removeEntry(entry));
        } catch (Exception e) {
            TLogger.getGlobalLogger().error("TagUtils.cleanEntryInScoreboard() 异常: " + e.toString());
        }
    }

    public static void cleanEmptyTeamInScoreboard(Scoreboard scoreboard) {
        try {
            scoreboard.getTeams().stream().filter(team -> team.getEntries().size() == 0).forEach(Team::unregister);
        } catch (Exception e) {
            TLogger.getGlobalLogger().error("TagUtils.cleanEmptyTeamInScoreboard() 异常: " + e.toString());
        }
    }

    public static Scoreboard getScoreboardComputeIfAbsent(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        if (scoreboard == null) {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
        return player.getScoreboard();
    }

    public static Scoreboard getScoreboardAndCleanTeams(Player player) {
        Scoreboard scoreboard = getScoreboardComputeIfAbsent(player);
        cleanTeamInScoreboard(scoreboard);
        return scoreboard;
    }

    public static Team getTeamComputeIfAbsent(Scoreboard scoreboard, String teamName) {
        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            scoreboard.registerNewTeam(teamName);
        }
        return scoreboard.getTeam(teamName);
    }
}
