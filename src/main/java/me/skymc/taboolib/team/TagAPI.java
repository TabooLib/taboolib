package me.skymc.taboolib.team;

import com.google.common.base.Preconditions;
import me.skymc.taboolib.Main;
import me.skymc.taboolib.itagapi.TagDataHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Set;

/**
 * @Author sky
 * @Since 2018-05-09 21:03
 */
@Deprecated
public class TagAPI {

    /**
     * 该工具于 2018年5月23日02:31:14 失效
     * 新工具类: {@link TagDataHandler}
     */

    TagAPI() {
    }

    public static void inst() {
    }

    public static String getPlayerDisplayName(Player player) {
        return TagDataHandler.getHandler().getDisplay(player);
    }

    public static void setPlayerDisplayName(Player player, String name) {
        TagDataHandler.getHandler().setDisplay(player, name);
    }

    public static void removePlayerDisplayName(Player player) {
        TagDataHandler.getHandler().setDisplay(player, player.getName());
    }

    public static void refreshPlayer(Player player) {
        Preconditions.checkState(Main.getInst().isEnabled(), "Not Enabled!");
        Preconditions.checkNotNull(player, "player");

        player.getWorld().getPlayers().forEach(playerFor -> refreshPlayer(player, playerFor));
    }

    public static void refreshPlayer(final Player player, final Player forWhom) {
        Preconditions.checkState(Main.getInst().isEnabled(), "Not Enabled!");
        Preconditions.checkNotNull(player, "player");
        Preconditions.checkNotNull(forWhom, "forWhom");

        if (player != forWhom && player.getWorld() == forWhom.getWorld() && forWhom.canSee(player)) {
            forWhom.hidePlayer(player);
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getInst(), () -> forWhom.showPlayer(player), 2);
        }
    }

    public static void refreshPlayer(Player player, Set<Player> forWhom) {
        Preconditions.checkState(Main.getInst().isEnabled(), "Not Enabled!");
        Preconditions.checkNotNull(player, "player");
        Preconditions.checkNotNull(forWhom, "forWhom");

        forWhom.forEach(playerFor -> refreshPlayer(player, playerFor));
    }
}
