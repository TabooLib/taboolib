package me.skymc.taboolib.anvil;

import com.ilummc.tlib.resources.TLocale;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author sky
 */
public class AnvilContainerAPI implements Listener {

    public static List<String> list = new ArrayList<>();
    public static ItemStack item = new ItemStack(Material.NAME_TAG);
    public static HashMap<String, String> isOpen = new HashMap<>();
    public static AnvilContainerAPIEvent event;

    public static void send(Player p, String type, String str, List<String> lorelist) {
        isOpen.put(p.getName(), type);

        AnvilContainer.openAnvil(p);
        ItemMeta meta = item.getItemMeta();

        list.clear();
        if (lorelist == null) {
            list.addAll(TLocale.asStringList("ANVIL-CONTAINER.LORE-NORMAL"));
        } else {
            list = lorelist;
        }

        meta.setLore(list);
        meta.setDisplayName(str);
        item.setItemMeta(meta);

        p.getOpenInventory().getTopInventory().setItem(0, item);
        p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);
    }

    @EventHandler
    public void close(InventoryCloseEvent e) {
        if (isOpen.containsKey(e.getPlayer().getName())) {
            isOpen.remove(e.getPlayer().getName());
            if (e.getInventory().getType() == InventoryType.ANVIL) {
                e.getInventory().clear();
            }
        }
    }

    @EventHandler
    public void click(InventoryClickEvent e) {
        if (!isOpen.containsKey(e.getWhoClicked().getName())) {
            return;
        }
        if (e.getInventory().getType() != InventoryType.ANVIL) {
            return;
        }
        e.setCancelled(true);

        int slot = e.getRawSlot();
        if (slot != 2) {
            return;
        }

        Inventory inv = e.getInventory();
        if (inv.getItem(2) == null) {
            return;
        }

        if (inv.getItem(2).getItemMeta().hasDisplayName()) {
            event = new AnvilContainerAPIEvent(e, isOpen.get(e.getWhoClicked().getName()), inv.getItem(2).getItemMeta().getDisplayName());
            e.getWhoClicked().closeInventory();
            Bukkit.getPluginManager().callEvent(event);
        }
    }

    @EventHandler
    public void example(PlayerCommandPreprocessEvent e) {
        if ("/anvilexample".equals(e.getMessage())) {
            if (e.getPlayer().hasPermission("taboolib.admin")) {
                e.setCancelled(true);
                AnvilContainerAPI.send(e.getPlayer(), "EXAMPLE", TLocale.asString("ANVIL-CONTAINER.NAME-EXAMPLE"), null);
            }
        }
    }

    @EventHandler
    public void example2(AnvilContainerAPIEvent e) {
        if ("EXAMPLE".equals(e.type)) {
            e.event.getWhoClicked().sendMessage(e.string);
        }
    }
}
