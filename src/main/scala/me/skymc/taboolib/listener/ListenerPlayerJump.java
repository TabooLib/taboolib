package me.skymc.taboolib.listener;

import me.skymc.taboolib.events.PlayerJumpEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;

@TListener
public class ListenerPlayerJump implements Listener {

    public HashMap<Player, Long> cooldown = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGH)
    public void onJump(PlayerMoveEvent event) {
        if ((!event.getPlayer().isFlying()) && (event.getPlayer().getGameMode() != GameMode.CREATIVE) && (event.getFrom().getY() + 0.5D != event.getTo().getY()) && (event.getFrom().getY() + 0.419D < event.getTo().getY())) {
            Location loc = event.getFrom();
            loc.setY(event.getFrom().getY() - 1.0D);
            if (loc.getBlock().getType() != Material.AIR) {
                if (!this.cooldown.containsKey(event.getPlayer())) {
                    this.cooldown.put(event.getPlayer(), System.currentTimeMillis() + 350L);
                    PlayerJumpEvent evt = new PlayerJumpEvent(event.isCancelled(), event.getPlayer());
                    Bukkit.getPluginManager().callEvent(evt);
                    if (evt.isCancelled()) {
                        event.setCancelled(true);
                    }
                } else if (this.cooldown.get(event.getPlayer()) <= System.currentTimeMillis()) {
                    this.cooldown.put(event.getPlayer(), System.currentTimeMillis() + 350L);
                    PlayerJumpEvent evt = new PlayerJumpEvent(event.isCancelled(), event.getPlayer());

                    Bukkit.getPluginManager().callEvent(evt);
                    if (evt.isCancelled()) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.cooldown.remove(event.getPlayer());
    }
}
