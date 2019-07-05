package io.izzel.taboolib.listener;

import io.izzel.taboolib.TabooLibAPI;
import io.izzel.taboolib.Version;
import io.izzel.taboolib.module.inject.TListener;
import io.izzel.taboolib.module.item.Items;
import io.izzel.taboolib.module.logger.TLogger;
import io.izzel.taboolib.module.tellraw.TellrawJson;
import io.izzel.taboolib.origin.database.PlayerDataManager;
import io.izzel.taboolib.origin.database.PluginDataManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

/**
 * @author sky
 */
@TListener
public class ListenerPlayerCommand implements Listener {

    @EventHandler
    public void cmd(ServerCommandEvent e) {
        if (e.getCommand().equalsIgnoreCase("saveFiles")) {
            if (Version.isAfter(Version.v1_8)) {
                e.setCancelled(true);
            }
            PluginDataManager.saveAllCaches();
            PlayerDataManager.saveAllCaches(true, false);
            TLogger.getGlobalLogger().info("Successfully.");
        } else if (e.getCommand().equalsIgnoreCase("tDebug")) {
            if (Version.isAfter(Version.v1_8)) {
                e.setCancelled(true);
            }
            if (TabooLibAPI.isDebug()) {
                TabooLibAPI.setDebug(false);
                TLogger.getGlobalLogger().info("&cDisabled.");
            } else {
                TabooLibAPI.setDebug(true);
                TLogger.getGlobalLogger().info("&aEnabled.");
            }
        } else if (e.getCommand().equalsIgnoreCase("tExceptionEvent")) {
            e.setCancelled(true);
            throw new IllegalStateException("TabooLib Example Exception");
        }
    }

    @EventHandler
    public void cmd(PlayerCommandPreprocessEvent e) {
        if (e.getMessage().equals("/tellrawTest") && e.getPlayer().hasPermission("taboolib.tellraw")) {
            e.setCancelled(true);
            TellrawJson.create()
                    .append("§8[§3§lTabooLib§8] §7TellrawJson Test: §f[")
                    .append(Items.getName(e.getPlayer().getItemInHand())).hoverItem(e.getPlayer().getItemInHand())
                    .append("§f]")
                    .send(e.getPlayer());
        }
    }
}
