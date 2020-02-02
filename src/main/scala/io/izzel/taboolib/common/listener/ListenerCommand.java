package io.izzel.taboolib.common.listener;

import io.izzel.taboolib.TabooLibAPI;
import io.izzel.taboolib.module.db.local.Local;
import io.izzel.taboolib.module.db.local.LocalPlayer;
import io.izzel.taboolib.module.inject.TListener;
import io.izzel.taboolib.module.locale.TLocale;
import io.izzel.taboolib.module.locale.logger.TLogger;
import io.izzel.taboolib.module.tellraw.TellrawJson;
import io.izzel.taboolib.util.Files;
import io.izzel.taboolib.util.item.Items;
import io.izzel.taboolib.util.lite.Signs;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.io.File;
import java.util.Arrays;

/**
 * @author sky
 */
@TListener
public class ListenerCommand implements Listener {

    @EventHandler
    public void cmd(PlayerCommandPreprocessEvent e) {
        if (e.getMessage().equalsIgnoreCase("/tabooLib")) {
            e.setCancelled(true);
            TLocale.Display.sendTitle(e.getPlayer(), "§fTabooLib", "§7TabooLib Enabled.");
        }
        if (e.getMessage().equalsIgnoreCase("/tellrawTest") && e.getPlayer().hasPermission("*")) {
            e.setCancelled(true);
            TellrawJson.create()
                    .append("§8[§3§lTabooLib§8] §7TellrawJson Test: §f[")
                    .append(Items.getName(e.getPlayer().getItemInHand())).hoverItem(e.getPlayer().getItemInHand())
                    .append("§f]")
                    .send(e.getPlayer());
        }
        if (e.getMessage().equalsIgnoreCase("/fakesignTest") && e.getPlayer().hasPermission("*")) {
            e.setCancelled(true);
            Signs.fakeSign(e.getPlayer(), lines -> {
                e.getPlayer().sendMessage("§8[§3§lTabooLib§8] §7FakeSign Lines: §f" + Arrays.toString(lines));
            });
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void cmd(ServerCommandEvent e) {
        if (e.getCommand().equalsIgnoreCase("saveFiles")) {
            Local.saveFiles();
            LocalPlayer.saveFiles();
            TLogger.getGlobalLogger().info("Successfully.");
        } else if (e.getCommand().equalsIgnoreCase("tDebug")) {
            if (TabooLibAPI.isDebug()) {
                TabooLibAPI.debug(false);
                TLogger.getGlobalLogger().info("&cDisabled.");
            } else {
                TabooLibAPI.debug(true);
                TLogger.getGlobalLogger().info("&aEnabled.");
            }
        } else if (e.getCommand().equalsIgnoreCase("libUpdate")) {
            e.setCancelled(true);
            e.getSender().sendMessage("§8[§fTabooLib§8] §cWARNING §7| §4Update TabooLib will force to restart your server. Please confirm this action by type §c/libupdateconfirm");
        } else if (e.getCommand().equalsIgnoreCase("libUpdateConfirm") || e.getCommand().equalsIgnoreCase("libUpdate confirm")) {
            e.getSender().sendMessage("§8[§fTabooLib§8] §7Downloading TabooLib file...");
            Files.downloadFile("https://skymc.oss-cn-shanghai.aliyuncs.com/plugins/TabooLib.jar", new File("libs/TabooLib.jar"));
            e.getSender().sendMessage("§8[§fTabooLib§8] §2Download completed, the server will restart in 3 secs");
            try {
                Thread.sleep(3000L);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            Bukkit.shutdown();
        }
    }
}
