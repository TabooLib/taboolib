package me.skymc.taboolib.commands.sub.sounds.listener;

import me.skymc.taboolib.inventory.InventoryUtil;
import me.skymc.taboolib.inventory.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;

/**
 * @author sky
 * @since 2018年2月4日 下午4:35:00
 */
public class SoundsLibraryPatch implements Listener {
	
	/**
	 * 打开物品库界面
	 *
	 * @param player
	 * @param page
	 */
	public static void openInventory(Player player, int page) {
		SoundLibraryHolder holder = new SoundLibraryHolder(page);
		Inventory inventory = Bukkit.createInventory(holder, 54, "音效库 " + page);

		int loop = 0;
		for (Sound sound : Arrays.asList(Sound.values())) {
			if (loop >= (page - 1) * 28) {
				if (loop < page * 28) {
					int slot = InventoryUtil.SLOT_OF_CENTENTS.get(loop - ((page - 1) * 28));
					ItemStack item = new ItemStack(Material.MAP);
					{
						ItemMeta meta = item.getItemMeta();
						meta.setDisplayName("§f" + sound.name());
						meta.setLore(Arrays.asList("", "§f左键: §71 音调", "§f左键: §72 音调"));
						item.setItemMeta(meta);
						inventory.setItem(slot, item);
					}
					holder.SOUNDS_DATA.put(slot, sound);
				} else {
					break;
				}
			}
			loop++;
		}

		if (page > 1) {
			inventory.setItem(47, ItemUtils.setName(new ItemStack(Material.ARROW), "§f上一页"));
		}
		if (((int) Math.ceil(Sound.values().length / 28D)) > page) {
			inventory.setItem(51, ItemUtils.setName(new ItemStack(Material.ARROW), "§f下一页"));
		}
		player.openInventory(inventory);
	}

	@EventHandler
	public void inventoryClick(InventoryClickEvent e) {
		if (e.getInventory().getHolder() instanceof SoundLibraryHolder) {
			e.setCancelled(true);

			if (e.getCurrentItem() == null || e.getCurrentItem().getType().equals(Material.AIR) || e.getRawSlot() >= e.getInventory().getSize()) {
				return;
			}

			switch (e.getRawSlot()) {
				case 47:
					openInventory((Player) e.getWhoClicked(), ((SoundLibraryHolder) e.getInventory().getHolder()).PAGE - 1);
					break;
				case 51:
					openInventory((Player) e.getWhoClicked(), ((SoundLibraryHolder) e.getInventory().getHolder()).PAGE + 1);
					break;
				default:
					Sound sound = ((SoundLibraryHolder) e.getInventory().getHolder()).SOUNDS_DATA.get(e.getRawSlot());
					if (e.getClick().isLeftClick()) {
						((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), sound, 1f, 1f);
					} else {
						((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), sound, 1f, 2f);
					}
					break;
			}
		}
	}
	
	public static class SoundLibraryHolder implements InventoryHolder {

		public final int PAGE;
		public final HashMap<Integer, Sound> SOUNDS_DATA = new HashMap<>();
			
		public SoundLibraryHolder(int page) {
			this.PAGE = page;
		}
			
		@Override
		public Inventory getInventory() {
			return null;
		}
	}
}
