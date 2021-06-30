package taboolib.module.ui;

import com.google.common.collect.Lists;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

/**
 * TabooLib
 * taboolib.module.ui.ItemUtils
 *
 * @author sky
 * @since 2021/7/1 1:04 上午
 */
public class ItemUtils {

    public static boolean isNull(ItemStack itemStack) {
        return itemStack == null || itemStack.getType().equals(Material.AIR);
    }

    public static boolean nonNull(ItemStack itemStack) {
        return itemStack != null && !itemStack.getType().equals(Material.AIR);
    }

    public static List<ItemStack> getAffectItemInClickEvent(InventoryClickEvent e) {
        List<ItemStack> list = Lists.newArrayList();
        if (e.getClick() == ClickType.NUMBER_KEY) {
            Optional.ofNullable(e.getWhoClicked().getInventory().getItem(e.getHotbarButton())).ifPresent(list::add);
        }
        Optional.ofNullable(e.getCurrentItem()).ifPresent(list::add);
        return list;
    }
}
