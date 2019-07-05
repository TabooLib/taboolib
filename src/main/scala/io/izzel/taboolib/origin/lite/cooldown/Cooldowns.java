package io.izzel.taboolib.origin.lite.cooldown;

import io.izzel.taboolib.module.inject.TListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.ConcurrentHashMap;

@TListener
public class Cooldowns implements Listener {

    private static ConcurrentHashMap<String, Cooldown> list = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String, Cooldown> getCooldownPacks() {
        return list;
    }

    public static void register(Cooldown pack) {
        list.put(pack.getPackName(), pack);
    }

    public static void register(Cooldown pack, Plugin plugin) {
        pack.setPlugin(plugin.getName());
        list.put(pack.getPackName(), pack);
    }

    public static void unregister(String name) {
        list.remove(name);
    }

    private static void unregister(Cooldown pack) {
        list.remove(pack.getPackName());
    }

    @EventHandler
    public void quit(PlayerQuitEvent e) {
        list.values().stream().filter(pack -> !pack.isCooldown(e.getPlayer().getName(), 0)).forEach(pack -> pack.unRegister(e.getPlayer().getName()));
    }

    @EventHandler
    public void disable(PluginDisableEvent e) {
        list.values().stream().filter(pack -> pack.getPlugin().equals(e.getPlugin().getName())).forEach(Cooldowns::unregister);
    }
}
