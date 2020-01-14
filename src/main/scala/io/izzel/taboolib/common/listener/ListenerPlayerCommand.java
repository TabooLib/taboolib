package io.izzel.taboolib.common.listener;

import io.izzel.taboolib.TabooLibAPI;
import io.izzel.taboolib.Version;
import io.izzel.taboolib.common.plugin.InternalPluginBridge;
import io.izzel.taboolib.module.db.local.Local;
import io.izzel.taboolib.module.db.local.LocalPlayer;
import io.izzel.taboolib.module.inject.TListener;
import io.izzel.taboolib.module.locale.TLocale;
import io.izzel.taboolib.module.locale.logger.TLogger;
import io.izzel.taboolib.module.tellraw.TellrawJson;
import io.izzel.taboolib.util.ArrayUtil;
import io.izzel.taboolib.util.item.Items;
import io.izzel.taboolib.util.lite.Signs;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.util.Arrays;

/**
 * @author sky
 */
@TListener
public class ListenerPlayerCommand implements Listener {

    @EventHandler
    public void cmd(PlayerCommandPreprocessEvent e) {
        if (e.getMessage().equalsIgnoreCase("/tabooLib")) {
            e.setCancelled(true);
            TLocale.Display.sendTitle(e.getPlayer(), "§fTabooLib", "§7TabooLib is enabled.");
        }
        if (e.getMessage().equalsIgnoreCase("/tellrawTest") && e.getPlayer().hasPermission("*")) {
            e.setCancelled(true);
            TellrawJson.create()
                    .append("§8[§3§lTabooLib§8] §7TellrawJson Test: §f[")
                    .append(Items.getName(e.getPlayer().getItemInHand())).hoverItem(e.getPlayer().getItemInHand())
                    .append("§f]")
                    .send(e.getPlayer());
        }
        if (e.getMessage().equalsIgnoreCase("/placeholderTest") && e.getPlayer().hasPermission("*")) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(InternalPluginBridge.handle().setPlaceholders(e.getPlayer(), "§8[§3§lTabooLib§8] §7PlaceholderAPI Test: §f%player_name%"));
        }
        if (e.getMessage().equalsIgnoreCase("/fakesignTest") && e.getPlayer().hasPermission("&")) {
            e.setCancelled(true);
            Signs.fakeSign(e.getPlayer(), ArrayUtil.asArray("§nFakeSign Test"), lines -> {
                e.getPlayer().sendMessage("§8[§3§lTabooLib§8] §7FakeSign Lines: §f" + Arrays.toString(lines));
            });
        }
    }

    @EventHandler
    public void cmd(ServerCommandEvent e) {
        if (e.getCommand().equalsIgnoreCase("saveFiles")) {
            if (Version.isAfter(Version.v1_8)) {
                e.setCancelled(true);
            }
            Local.saveFiles();
            LocalPlayer.saveFiles();
            TLogger.getGlobalLogger().info("Successfully.");
        } else if (e.getCommand().equalsIgnoreCase("tDebug")) {
            if (Version.isAfter(Version.v1_8)) {
                e.setCancelled(true);
            }
            if (TabooLibAPI.isDebug()) {
                TabooLibAPI.debug(false);
                TLogger.getGlobalLogger().info("&cDisabled.");
            } else {
                TabooLibAPI.debug(true);
                TLogger.getGlobalLogger().info("&aEnabled.");
            }
        }
    }
}
