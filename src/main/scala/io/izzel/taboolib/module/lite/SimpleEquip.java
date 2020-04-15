package io.izzel.taboolib.module.lite;

import io.izzel.taboolib.Version;
import org.bukkit.entity.LivingEntity;
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
    private boolean supported = Version.isAfter(Version.v1_9);

    SimpleEquip(EquipmentSlot bukkit, String nms, int slot) {
        this.bukkit = bukkit;
        this.nms = nms;
        this.slot = slot;
    }

    public void setItem(Player player, ItemStack item) {
        setItem((LivingEntity) player, item);
    }

    public void setItem(LivingEntity entity, ItemStack item) {
        switch (this) {
            case HAND:
                if (supported) {
                    entity.getEquipment().setItemInMainHand(item);
                } else {
                    entity.getEquipment().setItemInHand(item);
                }
                break;
            case OFF_HAND:
                if (supported) {
                    entity.getEquipment().setItemInOffHand(item);
                }
                break;
            case FEET:
                entity.getEquipment().setBoots(item);
                break;
            case LEGS:
                entity.getEquipment().setLeggings(item);
                break;
            case CHEST:
                entity.getEquipment().setChestplate(item);
                break;
            case HEAD:
                entity.getEquipment().setHelmet(item);
                break;
        }
    }

    public void setItemDropChance(LivingEntity entity, float chance) {
        switch (this) {
            case HAND:
                if (supported) {
                    entity.getEquipment().setItemInMainHandDropChance(chance);
                } else {
                    entity.getEquipment().setItemInHandDropChance(chance);
                }
                break;
            case OFF_HAND:
                if (supported) {
                    entity.getEquipment().setItemInOffHandDropChance(chance);
                }
                break;
            case FEET:
                entity.getEquipment().setBootsDropChance(chance);
                break;
            case LEGS:
                entity.getEquipment().setLeggingsDropChance(chance);
                break;
            case CHEST:
                entity.getEquipment().setChestplateDropChance(chance);
                break;
            case HEAD:
                entity.getEquipment().setHelmetDropChance(chance);
                break;
        }
    }

    public ItemStack getItem(Player player) {
        return getItem((LivingEntity) player);
    }

    public ItemStack getItem(LivingEntity entity) {
        switch (this) {
            case HAND:
                if (supported) {
                    return entity.getEquipment().getItemInMainHand();
                } else {
                    return entity.getEquipment().getItemInHand();
                }
            case OFF_HAND:
                if (supported) {
                    return entity.getEquipment().getItemInOffHand();
                }
            case FEET:
                return entity.getEquipment().getBoots();
            case LEGS:
                return entity.getEquipment().getLeggings();
            case CHEST:
                return entity.getEquipment().getChestplate();
            case HEAD:
                return entity.getEquipment().getHelmet();
            default:
                return null;
        }
    }

    public float getItemDropChance(LivingEntity entity) {
        switch (this) {
            case HAND:
                if (supported) {
                    return entity.getEquipment().getItemInMainHandDropChance();
                } else {
                    return entity.getEquipment().getItemInHandDropChance();
                }
            case OFF_HAND:
                if (supported) {
                    return entity.getEquipment().getItemInOffHandDropChance();
                }
            case FEET:
                return entity.getEquipment().getBootsDropChance();
            case LEGS:
                return entity.getEquipment().getLeggingsDropChance();
            case CHEST:
                return entity.getEquipment().getChestplateDropChance();
            case HEAD:
                return entity.getEquipment().getHelmetDropChance();
            default:
                return 0f;
        }
    }

    public static SimpleEquip fromNMS(String nms) {
        return Arrays.stream(values()).filter(tEquipment -> tEquipment.nms.equalsIgnoreCase(nms)).findFirst().orElse(null);
    }

    public static SimpleEquip fromBukkit(EquipmentSlot bukkit) {
        return Arrays.stream(values()).filter(tEquipment -> tEquipment.bukkit == bukkit).findFirst().orElse(null);
    }

    public static Map<SimpleEquip, ItemStack> getItems(Player player) {
        return getItems((LivingEntity) player);
    }

    public static Map<SimpleEquip, ItemStack> getItems(LivingEntity entity) {
        return Arrays.stream(values()).collect(Collectors.toMap(equipment -> equipment, equipment -> equipment.getItem(entity), (a, b) -> b));
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
