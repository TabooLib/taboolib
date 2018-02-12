package me.skymc.taboolib.commands.sub.itemlist.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

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

import me.skymc.taboolib.inventory.InventoryUtil;
import me.skymc.taboolib.inventory.ItemUtils;

/**
 * @author sky
 * @since 2018年2月4日 下午4:35:00
 */
public class ItemLibraryPatch implements Listener {
	
	@EventHandler
	public void inventoryClick(InventoryClickEvent e) {
		if (e.getInventory().getHolder() instanceof ItemLibraryHolder) {
			e.setCancelled(true);
			
			if (e.getCurrentItem() == null || e.getCurrentItem().getType().equals(Material.AIR)) {
				return;
			}
			
			if (e.getRawSlot() == 47) {
				openInventory((Player) e.getWhoClicked(), ((ItemLibraryHolder) e.getInventory().getHolder()).PAGE - 1);
			}
			else if (e.getRawSlot() == 51) {
				openInventory((Player) e.getWhoClicked(), ((ItemLibraryHolder) e.getInventory().getHolder()).PAGE + 1);
			}
			else {
				e.getWhoClicked().getInventory().addItem(ItemUtils.getCacheItem(((ItemLibraryHolder) e.getInventory().getHolder()).ITEMS_DATA.get(e.getRawSlot())));
			}
		}
	}
	
	/**
	 * 打开物品库界面
	 * 
	 * @param player
	 * @param page
	 */
	public static void openInventory(Player player, int page) {
		ItemLibraryHolder holder = new ItemLibraryHolder(page);
		Inventory inventory = Bukkit.createInventory(holder, 54, "物品库");
		
		LinkedHashMap<String, ItemStack> map = new LinkedHashMap<>();
		map.putAll(ItemUtils.getItemCachesFinal());
		map.putAll(ItemUtils.getItemCaches());
		
		int loop = 0;
		Iterator<String> iterator = map.keySet().iterator();
		while (iterator.hasNext()) {
			String name = iterator.next();
			if (loop >= (page - 1) * 28) {
				if (loop < page * 28) {
					int slot = InventoryUtil.SLOT_OF_CENTENTS.get(loop - ((page - 1) * 28));
					ItemStack item = map.get(name).clone(); {
						ItemMeta meta = item.getItemMeta();
						List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
						lore.add("§f");
						lore.add("§f§m                  ");
						lore.add("§f序列号: §8" + name);
						meta.setLore(lore);
						item.setItemMeta(meta);
						inventory.setItem(slot, item);
					}
					holder.ITEMS_DATA.put(slot, name);
				}
				else {
					break;
				}
			}
			loop++;
		}
		
		if (page > 1) {
			inventory.setItem(47, ItemUtils.setName(new ItemStack(Material.ARROW), "§f上一页"));
		}
		if (((int) Math.ceil(ItemUtils.getItemCaches().size() / 28D)) > page) {
			inventory.setItem(51, ItemUtils.setName(new ItemStack(Material.ARROW), "§f下一页"));
		}
		player.openInventory(inventory);
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
