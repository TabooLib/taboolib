package me.skymc.taboolib.listener;

import com.ilummc.tlib.logger.TLogger;
import me.skymc.taboolib.Main;
import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.database.PlayerDataManager;
import me.skymc.taboolib.inventory.ItemUtils;
import me.skymc.taboolib.itemnbtapi.NBTItem;
import me.skymc.taboolib.json.tellraw.TellrawJson;
import me.skymc.taboolib.message.MsgUtils;
import me.skymc.taboolib.permission.PermissionUtils;
import me.skymc.taboolib.playerdata.DataUtils;
import org.bukkit.Bukkit;
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
            if (TabooLib.getVerint() > 10700) {
                e.setCancelled(true);
            }
            Bukkit.getScheduler().runTask(Main.getInst(), DataUtils::saveAllCaches);
            Bukkit.getScheduler().runTask(Main.getInst(), () -> PlayerDataManager.saveAllCaches(true, false));
            TLogger.getGlobalLogger().info("Successfully.");
        } else if (e.getCommand().equalsIgnoreCase("tDebug")) {
            if (TabooLib.getVerint() > 10700) {
                e.setCancelled(true);
            }
            if (TabooLib.isDebug()) {
                TabooLib.setDebug(false);
                TLogger.getGlobalLogger().info("&cDisabled.");
            } else {
                TabooLib.setDebug(true);
                TLogger.getGlobalLogger().info("&aEnabled.");
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void cmd(PlayerCommandPreprocessEvent e) {
        if (e.getMessage().equals("/unbreakable") && PermissionUtils.hasPermission(e.getPlayer(), "taboolib.unbreakable")) {
            e.setCancelled(true);
            NBTItem nbt = new NBTItem(e.getPlayer().getItemInHand());
            nbt.setInteger("Unbreakable", 1);
            e.getPlayer().setItemInHand(nbt.getItem());
            MsgUtils.send(e.getPlayer(), "Success!");
        } else if (e.getMessage().equals("/tellrawTest") && PermissionUtils.hasPermission(e.getPlayer(), "taboolib.tellraw")) {
            e.setCancelled(true);
            TellrawJson.create()
                    .append("§8[§3§lTabooLib§8] §7TellrawJson Test: §f[")
                    .append(ItemUtils.getCustomName(e.getPlayer().getItemInHand())).hoverItem(e.getPlayer().getItemInHand())
                    .append("§f]")
                    .send(e.getPlayer());
        }
    }
}
