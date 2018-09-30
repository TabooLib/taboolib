package me.skymc.taboolib.skull;

import com.google.common.annotations.Beta;
import me.skymc.taboolib.inventory.builder.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

@Beta
@Deprecated
public class SkullUtils {

    public static ItemStack getItem(OfflinePlayer p) {
        return new ItemBuilder(p).build();
    }

    public static ItemStack getOnlineItem(OfflinePlayer p) {
        return p.isOnline() ? new ItemBuilder(p).build() : new ItemStack(Material.SKULL_ITEM);
    }

}
