package me.skymc.taboolib.listener;

import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.Main;
import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.playerdata.DataUtils;
import me.skymc.taboolib.translateuuid.TranslateUUID;
import me.skymc.taboolib.update.UpdateTask;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ListenerPlayerJoinAndQuit implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (UpdateTask.isHaveUpdate() && e.getPlayer().hasPermission("taboolib.update.notify")) {
            TLocale.Logger.sendTo(e.getPlayer(), "UPDATETASK.VERSION-OUTDATED", String.valueOf(TabooLib.getPluginVersion()), String.valueOf(UpdateTask.getNewVersion()));
        }
        if (TranslateUUID.isEnabled()) {
            Bukkit.getScheduler().runTaskAsynchronously(Main.getInst(), () -> TranslateUUID.updateUsername(e.getPlayer().getUniqueId(), e.getPlayer().getName()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        DataUtils.saveOnline(e.getPlayer().getName());
    }
}
