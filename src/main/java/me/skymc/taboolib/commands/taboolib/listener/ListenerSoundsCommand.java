package me.skymc.taboolib.commands.taboolib.listener;

import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.inventory.InventoryUtil;
import me.skymc.taboolib.inventory.ItemUtils;
import me.skymc.taboolib.listener.TListener;
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
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author sky
 * @since 2018年2月4日 下午4:35:00
 */
@TListener
public class ListenerSoundsCommand implements Listener {

    public static void openInventory(Player player, int page, String search) {
        if (page < 1) {
            page = 1;
        }

        SoundLibraryHolder holder = new SoundLibraryHolder(page, search);
        Inventory inventory = Bukkit.createInventory(holder, 54, TLocale.asString("COMMANDS.TABOOLIB.SOUNDS.MENU.TITLE", String.valueOf(page)));
        List<Sound> soundFilter = Arrays.stream(Sound.values()).filter(sound -> search == null || sound.name().contains(search.toUpperCase())).collect(Collectors.toList());
        List<String> soundLore = TLocale.asStringList("COMMANDS.TABOOLIB.SOUNDS.MENU.LORE");

        int loop = 0;
        for (Sound sound : soundFilter) {
            if (loop >= (page - 1) * 28) {
                if (loop < page * 28) {
                    int slot = InventoryUtil.SLOT_OF_CENTENTS.get(loop - ((page - 1) * 28));
                    ItemStack item = new ItemStack(Material.MAP);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName("§f§n" + sound);
                    meta.setLore(soundLore);
                    item.setItemMeta(meta);
                    inventory.setItem(slot, item);
                    holder.SOUNDS_DATA.put(slot, sound);
                } else {
                    break;
                }
            }
            loop++;
        }

        if (page > 1) {
            inventory.setItem(47, ItemUtils.setName(new ItemStack(Material.ARROW), TLocale.asString("COMMANDS.TABOOLIB.SOUNDS.MENU.BACK")));
        }
        if (((int) Math.ceil(Sound.values().length / 28D)) > page) {
            inventory.setItem(51, ItemUtils.setName(new ItemStack(Material.ARROW), TLocale.asString("COMMANDS.TABOOLIB.SOUNDS.MENU.NEXT")));
        }

        if (!(player.getOpenInventory().getTopInventory().getHolder() instanceof SoundLibraryHolder)) {
            TLocale.sendTo(player, "COMMANDS.TABOOLIB.SOUNDS.RESULT.SEARCH", (search == null ? "*" : search), String.valueOf(soundFilter.size()));
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

            SoundLibraryHolder soundLibraryHolder = ((SoundLibraryHolder) e.getInventory().getHolder());
            int i = e.getRawSlot();
            if (i == 47) {
                openInventory((Player) e.getWhoClicked(), soundLibraryHolder.PAGE - 1, soundLibraryHolder.SEARCH);
            } else if (i == 51) {
                openInventory((Player) e.getWhoClicked(), soundLibraryHolder.PAGE + 1, soundLibraryHolder.SEARCH);
            } else {
                Sound sound = soundLibraryHolder.SOUNDS_DATA.get(e.getRawSlot());
                if (e.getClick().isLeftClick()) {
                    ((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), sound, 1f, 1f);
                } else if (e.getClick().isRightClick()) {
                    ((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), sound, 1f, 2f);
                } else if (e.getClick().isCreativeAction()) {
                    TLocale.sendTo(e.getWhoClicked(), "COMMANDS.TABOOLIB.SOUNDS.RESULT.COPY", sound.name());
                }
            }
        }
    }

    public static class SoundLibraryHolder implements InventoryHolder {

        public final int PAGE;
        public final String SEARCH;
        public final HashMap<Integer, Sound> SOUNDS_DATA = new HashMap<>();

        public SoundLibraryHolder(int page, String search) {
            this.PAGE = page;
            this.SEARCH = search;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
