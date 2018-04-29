package me.skymc.taboolib.display;

import com.ilummc.tlib.nms.ActionBar;
import org.bukkit.entity.Player;

/**
 * @author Bkm016
 * @since 2018-04-26
 */
public class ActionUtils {

    public static void send(Player player, String action) {
        if (player == null)
            return;
        try {
            ActionBar.sendActionBar(player, action);
        } catch (Throwable ignored) {
        }
    }
}

