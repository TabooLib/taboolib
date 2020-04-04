package io.izzel.taboolib.module.lite;

import io.izzel.taboolib.module.i18n.I18n;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Optional;

/**
 * @Author 坏黑
 * @Since 2019-05-22 1:16
 */
public class SimpleI18n {

    public static String getCustomName(Entity entity) {
        return entity != null ? Optional.ofNullable(entity.getCustomName()).orElse(getName(entity)) : getName(entity);
    }

    public static String getCustomName(ItemStack item) {
        if (item != null) {
            ItemMeta itemMeta = item.getItemMeta();
            return itemMeta != null && itemMeta.hasDisplayName() ? itemMeta.getDisplayName() : getName(item);
        }
        return getName(item);
    }

    public static String getName(Entity entity) {
        return I18n.get().getName(null, entity);
    }

    public static String getName(ItemStack item) {
        return I18n.get().getName(null, item);
    }

    public static String getName(Player player, Entity entity) {
        return I18n.get().getName(player, entity);
    }

    public static String getName(Player player, ItemStack item) {
        return I18n.get().getName(player, item);
    }
}
