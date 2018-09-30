package me.skymc.taboolib.skull;

import me.skymc.taboolib.inventory.builder.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

@Deprecated
public class SkullUtils {

    public static ItemStack getItme(OfflinePlayer p) {
        return new ItemBuilder(p).build();
    }

    public static ItemStack getOnlineItem(OfflinePlayer p) {
        return p.isOnline() ? new ItemBuilder(p).build() : new ItemStack(Material.SKULL_ITEM);
    }

}
