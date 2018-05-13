package me.skymc.taboolib.commands.taboolib.listener;

import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.inventory.InventoryUtil;
import me.skymc.taboolib.inventory.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author sky
 * @since 2018年2月4日 下午4:35:00
 */
public class ListenerItemListCommand implements Listener {

    /**
     * 打开物品库界面
     *
     * @param player
     * @param page
     */
    public static void openInventory(Player player, int page) {
        ItemLibraryHolder holder = new ItemLibraryHolder(page);
        Inventory inventory = Bukkit.createInventory(holder, 54, TLocale.asString("COMMANDS.TABOOLIB.ITEMLIST.MENU.TITLE", String.valueOf(page)));

        LinkedHashMap<String, ItemStack> map = new LinkedHashMap<>();
        map.putAll(ItemUtils.getItemCachesFinal());
        map.putAll(ItemUtils.getItemCaches());

        int loop = 0;
        for (String name : map.keySet()) {
            if (loop >= (page - 1) * 28) {
                if (loop < page * 28) {
                    int slot = InventoryUtil.SLOT_OF_CENTENTS.get(loop - ((page - 1) * 28));
                    ItemStack item = map.get(name).clone();
                    ItemMeta meta = item.getItemMeta();
                    List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
                    lore.addAll(TLocale.asStringList("COMMANDS.TABOOLIB.ITEMLIST.MENU.LORE", name));
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                    inventory.setItem(slot, item);
                    holder.ITEMS_DATA.put(slot, name);
                } else {
                    break;
                }
            }
            loop++;
        }

        if (page > 1) {
            inventory.setItem(47, ItemUtils.setName(new ItemStack(Material.ARROW), TLocale.asString("COMMANDS.TABOOLIB.ITEMLIST.MENU.BACK")));
        }
        if (((int) Math.ceil(map.size() / 28D)) > page) {
            inventory.setItem(51, ItemUtils.setName(new ItemStack(Material.ARROW), TLocale.asString("COMMANDS.TABOOLIB.ITEMLIST.MENU.NEXT")));
        }
        player.openInventory(inventory);
    }

    @EventHandler
    public void inventoryClick(InventoryClickEvent e) {
        if (e.getInventory().getHolder() instanceof ItemLibraryHolder) {
            e.setCancelled(true);

            if (e.getCurrentItem() == null || e.getCurrentItem().getType().equals(Material.AIR) || e.getRawSlot() >= e.getInventory().getSize()) {
                return;
            }

            int i = e.getRawSlot();
            if (i == 47) {
                openInventory((Player) e.getWhoClicked(), ((ItemLibraryHolder) e.getInventory().getHolder()).PAGE - 1);
            } else if (i == 51) {
                openInventory((Player) e.getWhoClicked(), ((ItemLibraryHolder) e.getInventory().getHolder()).PAGE + 1);
            } else {
                e.getWhoClicked().getInventory().addItem(ItemUtils.getCacheItem(((ItemLibraryHolder) e.getInventory().getHolder()).ITEMS_DATA.get(e.getRawSlot())));
            }
        }
    }

    public static class ItemLibraryHolder implements InventoryHolder {

        public final int PAGE;
        public final HashMap<Integer, String> ITEMS_DATA = new HashMap<>();

        public ItemLibraryHolder(int page) {
            this.PAGE = page;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
