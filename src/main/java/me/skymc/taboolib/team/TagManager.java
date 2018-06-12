package me.skymc.taboolib.team;

import com.ilummc.tlib.util.Strings;
import me.skymc.taboolib.Main;
import me.skymc.taboolib.itagapi.TagDataHandler;
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
@Deprecated
public class TagManager {

    /**
     * 该工具于 2018年5月23日02:31:14 失效
     * 新工具类: {@link TagDataHandler}
     */

    private static TagManager inst;

    public static TagManager getInst() {
        synchronized (TagManager.class) {
            if (inst == null) {
                inst = new TagManager();
            }
        }
        return inst;
    }

    public HashMap<UUID, PlayerData> getPlayerData() {
        return new HashMap<>(0);
    }

    public void setPrefix(Player player, String prefix) {
        TagDataHandler.getHandler().setPrefix(player, prefix);
    }

    public void setSuffix(Player player, String suffix) {
        TagDataHandler.getHandler().setSuffix(player, suffix);
    }

    public String getPrefix(Player player) {
        return TagDataHandler.getHandler().getPrefix(player);
    }

    public String getSuffix(Player player) {
        return TagDataHandler.getHandler().getSuffix(player);
    }

    public PlayerData getPlayerData(Player player) {
        return new PlayerData(player);
    }

    public void unloadData(Player targetPlayer) {
        TagDataHandler.getHandler().resetVariable(targetPlayer);
    }

    public void uploadData(Player targetPlayer) {
    }

    public void downloadData(Player targetPlayer) {
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
