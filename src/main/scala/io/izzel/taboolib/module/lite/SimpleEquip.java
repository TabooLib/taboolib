package io.izzel.taboolib.module.lite;

import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author 坏黑
 * @Since 2019-04-25 22:01
 */
public enum SimpleEquip {

    HAND(EquipmentSlot.HAND, "mainhand", -1),

    OFF_HAND(EquipmentSlot.OFF_HAND, "offhand", 40),

    FEET(EquipmentSlot.FEET, "feet", 36),

    LEGS(EquipmentSlot.LEGS, "legs", 37),

    CHEST(EquipmentSlot.CHEST, "chest", 38),

    HEAD(EquipmentSlot.HEAD, "head", 39);

    private EquipmentSlot bukkit;
    private String nms;
    private int slot;

    SimpleEquip(EquipmentSlot bukkit, String nms, int slot) {
        this.bukkit = bukkit;
        this.nms = nms;
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

    public static SimpleEquip fromNMS(String nms) {
        return Arrays.stream(values()).filter(tEquipment -> tEquipment.nms.equalsIgnoreCase(nms)).findFirst().orElse(null);
    }

    public static SimpleEquip fromBukkit(EquipmentSlot bukkit) {
        return Arrays.stream(values()).filter(tEquipment -> tEquipment.bukkit == bukkit).findFirst().orElse(null);
    }

    public static Map<SimpleEquip, ItemStack> getItems(Player player) {
        return Arrays.stream(values()).collect(Collectors.toMap(equipment -> equipment, equipment -> equipment.getItem(player), (a, b) -> b));
    }

    // *********************************
    //
    //        Getter and Setter
    //
    // *********************************

    public EquipmentSlot getBukkit() {
        return bukkit;
    }

    public String getNMS() {
        return nms;
    }

    public int getSlot() {
        return slot;
    }
}
