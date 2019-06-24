package me.skymc.taboolib.inventory.builder.v2;

import me.skymc.taboolib.listener.TListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

/**
 * @Author 坏黑
 * @Since 2019-05-21 22:04
 */
@TListener(version = ">=10900")
class ClickListener1_9 implements Listener {

    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent e) {
        if (e.getPlayer().getOpenInventory().getTopInventory().getHolder() instanceof MenuHolder && ((MenuHolder) e.getPlayer().getOpenInventory().getTopInventory().getHolder()).getBuilder().isLockHand()) {
            e.setCancelled(true);
        }
    }

}
