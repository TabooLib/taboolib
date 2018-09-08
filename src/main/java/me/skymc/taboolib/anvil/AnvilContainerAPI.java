package me.skymc.taboolib.anvil;

import com.ilummc.tlib.util.asm.AsmClassLoader;
import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.listener.TListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

/**
 * @author sky
 */
@TListener
public class AnvilContainerAPI implements Listener {

    private static Class<?> impl;

    public AnvilContainerAPI() {
        try {
            impl = AsmClassLoader.createNewClass("me.skymc.taboolib.anvil.AnvilContainer", AnvilContainerAsm.create(TabooLib.getVersion()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void openAnvil(Player player) {
        try {
            impl.getMethod("openAnvil", Player.class).invoke(impl, player);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void example(PlayerCommandPreprocessEvent e) {
        if (e.getMessage().equalsIgnoreCase("/anvilExample") && e.getPlayer().hasPermission("taboolib.admin")) {
            e.setCancelled(true);
            openAnvil(e.getPlayer());
        }
    }
}
