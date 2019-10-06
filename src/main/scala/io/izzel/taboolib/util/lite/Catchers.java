package io.izzel.taboolib.util.lite;

import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.module.inject.PlayerContainer;
import io.izzel.taboolib.module.inject.TListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.LinkedList;

@TListener
public class Catchers implements Listener {

    @PlayerContainer
    private static HashMap<String, LinkedList<Catcher>> playerdata = new HashMap<>();

    public static HashMap<String, LinkedList<Catcher>> getPlayerdata() {
        return playerdata;
    }

    public static boolean contains(Player player) {
        return playerdata.containsKey(player.getName()) && playerdata.get(player.getName()).size() > 0;
    }

    public static void call(Player player, Catcher catcher) {
        if (!playerdata.containsKey(player.getName())) {
            playerdata.put(player.getName(), new LinkedList<>());
        }
        playerdata.get(player.getName()).add(catcher.before());
    }

    @EventHandler
    public void chat(AsyncPlayerChatEvent e) {
        if (playerdata.containsKey(e.getPlayer().getName()) && contains(e.getPlayer())) {
            e.setCancelled(true);
            // 1.14 supported.
            Bukkit.getScheduler().runTask(TabooLib.getPlugin(), () -> {
                // 退出
                if (e.getMessage().equalsIgnoreCase("quit()")) {
                    playerdata.get(e.getPlayer().getName()).removeFirst().cancel();
                }
                // 默认
                else {
                    Catcher catcher = playerdata.get(e.getPlayer().getName()).getFirst();
                    // 如果终止引导
                    if (!catcher.after(e.getMessage())) {
                        playerdata.get(e.getPlayer().getName()).removeFirst();
                    } else {
                        catcher.before();
                    }
                }
            });
        }
    }

    public interface Catcher {

        default Catcher before() {
            return this;
        }

        boolean after(String message);

        default void cancel() {
        }
    }
}
