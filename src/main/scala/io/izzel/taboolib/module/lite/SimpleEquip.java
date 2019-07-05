package io.izzel.taboolib.module.lite;

import com.google.common.collect.Maps;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Map;

/**
 * @Author 坏黑
 * @Since 2019-04-25 22:01
 */
public enum SimpleEquip {

    HAND(EquipmentSlot.HAND, -1),

    OFF_HAND(EquipmentSlot.OFF_HAND, 40),

    FEET(EquipmentSlot.FEET, 36),

    LEGS(EquipmentSlot.LEGS, 37),

    CHEST(EquipmentSlot.CHEST, 38),

    HEAD(EquipmentSlot.HEAD, 39);

    private EquipmentSlot bukkit;
    private int slot;

    SimpleEquip(EquipmentSlot bukkit, int slot) {
        this.bukkit = bukkit;
        this.slot = slot;
    }

    public void setItem(Player player, ItemStack item) {
        if (this != HAND) {
            player.getInventory().setItem(slot, item);
        } else {
            player.setItemInHand(item);
        }
    }

    public ItemStack getItem(Player player) {
        if (this != HAND) {
            return player.getInventory().getItem(slot);
        } else {
            return player.getItemInHand();
        }
    }

    public static SimpleEquip fromBukkit(EquipmentSlot bukkit) {
        return Arrays.stream(values()).filter(tEquipment -> tEquipment.bukkit == bukkit).findFirst().orElse(null);
    }

    public static Map<SimpleEquip, ItemStack> getItems(Player player) {
        Map<SimpleEquip, ItemStack> map = Maps.newHashMap();
        for (SimpleEquip equipment : values()) {
            map.put(equipment, equipment.getItem(player));
        }
        return map;
    }

    // *********************************
    //
    //        Getter and Setter
    //
    // *********************************

    public EquipmentSlot getBukkit() {
        return bukkit;
    }

    public int getSlot() {
        return slot;
    }
}
