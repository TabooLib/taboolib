package me.skymc.taboolib.listener;

import me.skymc.taboolib.Main;
import me.skymc.taboolib.playerdata.DataUtils;
import me.skymc.taboolib.translateuuid.TranslateUUID;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@TListener
public class ListenerPlayerJoinAndQuit implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (TranslateUUID.isEnabled()) {
            Bukkit.getScheduler().runTaskAsynchronously(Main.getInst(), () -> TranslateUUID.updateUsername(e.getPlayer().getUniqueId(), e.getPlayer().getName()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        DataUtils.saveOnline(e.getPlayer().getName());
    }
}
