package taboolib.module.ui;

import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import taboolib.common.platform.Platform;
import taboolib.common.platform.PlatformSide;
import taboolib.common.platform.SubscribeEvent;

@PlatformSide(Platform.BUKKIT)
public class ClickListenerOffhand {

    public static ClickListenerOffhand INSTANCE = new ClickListenerOffhand();

    @SubscribeEvent
    public void onSwap(PlayerSwapHandItemsEvent e) {
        if (e.getPlayer().getOpenInventory().getTopInventory().getHolder() instanceof MenuHolder && ((MenuHolder) e.getPlayer().getOpenInventory().getTopInventory().getHolder()).getBuilder().isLockHand()) {
            e.setCancelled(true);
        }
    }
}
