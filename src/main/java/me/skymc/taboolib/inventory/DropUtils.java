package me.skymc.taboolib.inventory;

import me.skymc.taboolib.entity.VectorUtils;
import me.skymc.taboolib.other.NumberUtils;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.stream.IntStream;

@Deprecated
public class DropUtils {

    public static Item drop(Player player, ItemStack itemStack, double bulletSpread, double radius) {
        return VectorUtils.itemDrop(player, itemStack, bulletSpread, radius);
    }

}
