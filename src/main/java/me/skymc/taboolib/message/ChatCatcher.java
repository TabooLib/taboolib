package me.skymc.taboolib.message;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.LinkedList;

public class ChatCatcher implements Listener {

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
    public void quit(PlayerQuitEvent e) {
        playerdata.remove(e.getPlayer().getName());
    }

    @EventHandler
    public void chat(AsyncPlayerChatEvent e) {
        if (playerdata.containsKey(e.getPlayer().getName()) && playerdata.get(e.getPlayer().getName()).size() > 0) {
            e.setCancelled(true);

            if (e.getMessage().equalsIgnoreCase("quit()")) {
                // 退出引导
                playerdata.get(e.getPlayer().getName()).removeFirst().cancel();
                // 清理数据
                clearData(e.getPlayer());
            } else {
                Catcher catcher = playerdata.get(e.getPlayer().getName()).getFirst();
                // 如果终止引导
                if (!catcher.after(e.getMessage())) {
                    // 移除
                    playerdata.get(e.getPlayer().getName()).removeFirst();
                    // 清理
                    clearData(e.getPlayer());
                } else {
                    catcher.before();
                }
            }
        }
    }

    private void clearData(Player player) {
        if (playerdata.containsKey(player.getName()) && playerdata.get(player.getName()).size() == 0) {
            playerdata.remove(player.getName());
        }
    }

    public interface Catcher {

        Catcher before();

        boolean after(String message);

        void cancel();
    }
}
