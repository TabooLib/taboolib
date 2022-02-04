package taboolib.type;

import com.google.common.base.Enums;
import com.google.common.collect.Maps;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import taboolib.common.Isolated;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

/**
 * 装备类型转换工具
 *
 * @author 坏黑
 * @since 2019-04-25 22:01
 */
@Isolated
public enum BukkitEquipment {

    /**
     * 主手
     */
    HAND(EquipmentSlot.HAND, "mainhand", -1),

    /**
     * 副手
     */
    OFF_HAND(Enums.getIfPresent(EquipmentSlot.class, "OFF_HAND").or(EquipmentSlot.HAND), "offhand", 40),

    /**
     * 脚
     */
    FEET(EquipmentSlot.FEET, "feet", 36),

    /**
     * 腿
     */
    LEGS(EquipmentSlot.LEGS, "legs", 37),

    /**
     * 胸
     */
    CHEST(EquipmentSlot.CHEST, "chest", 38),

    /**
     * 头
     */
    HEAD(EquipmentSlot.HEAD, "head", 39);

    private final EquipmentSlot bukkit;
    private final String nms;
    private final int slot;

    BukkitEquipment(EquipmentSlot bukkit, String nms, int slot) {
        this.bukkit = bukkit;
        this.nms = nms;
        this.slot = slot;
    }

    /**
     * 设置物品
     *
     * @param player 玩家实例
     * @param item   物品
     */
    public void setItem(Player player, ItemStack item) {
        setItem((LivingEntity) player, item);
    }

    /**
     * 设置物品
     *
     * @param entity 实体实例
     * @param item   物品
     */
    public void setItem(LivingEntity entity, ItemStack item) {
        EntityEquipment equipment = entity.getEquipment();
        if (equipment == null) {
            return;
        }
        switch (this) {
            case HAND:
                try {
                    equipment.setItemInMainHand(item);
                } catch (NoSuchMethodError ex) {
                    equipment.setItemInHand(item);
                }
                break;
            case OFF_HAND:
                equipment.setItemInOffHand(item);
                break;
            case FEET:
                equipment.setBoots(item);
                break;
            case LEGS:
                equipment.setLeggings(item);
                break;
            case CHEST:
                equipment.setChestplate(item);
                break;
            case HEAD:
                equipment.setHelmet(item);
                break;
        }
    }

    /**
     * 设置物品掉落几率
     *
     * @param entity 实体实例
     * @param chance 几率
     */
    public void setItemDropChance(LivingEntity entity, float chance) {
        EntityEquipment equipment = entity.getEquipment();
        if (equipment == null) {
            return;
        }
        switch (this) {
            case HAND:
                try {
                    equipment.setItemInMainHandDropChance(chance);
                } catch (NoSuchMethodError ex) {
                    equipment.setItemInHandDropChance(chance);
                }
                break;
            case OFF_HAND:
                equipment.setItemInOffHandDropChance(chance);
                break;
            case FEET:
                equipment.setBootsDropChance(chance);
                break;
            case LEGS:
                equipment.setLeggingsDropChance(chance);
                break;
            case CHEST:
                equipment.setChestplateDropChance(chance);
                break;
            case HEAD:
                equipment.setHelmetDropChance(chance);
                break;
        }
    }

    /**
     * 获取物品
     *
     * @param player 玩家实例
     * @return ItemStack
     */
    @Nullable
    public ItemStack getItem(Player player) {
        return getItem((LivingEntity) player);
    }

    /**
     * 获取物品
     *
     * @param entity 玩家实例
     * @return ItemStack
     */
    @Nullable
    public ItemStack getItem(LivingEntity entity) {
        EntityEquipment equipment = entity.getEquipment();
        if (equipment == null) {
            return null;
        }
        switch (this) {
            case HAND:
                try {
                    return equipment.getItemInMainHand();
                } catch (NoSuchMethodError ex) {
                    return equipment.getItemInHand();
                }
            case OFF_HAND:
                return equipment.getItemInOffHand();
            case FEET:
                return equipment.getBoots();
            case LEGS:
                return equipment.getLeggings();
            case CHEST:
                return equipment.getChestplate();
            case HEAD:
                return equipment.getHelmet();
            default:
                return null;
        }
    }

    /**
     * 获取物品掉落几率
     *
     * @param entity 实体实例
     * @return float
     */
    public float getItemDropChance(LivingEntity entity) {
        EntityEquipment equipment = entity.getEquipment();
        if (equipment == null) {
            return 0f;
        }
        switch (this) {
            case HAND:
                try {
                    return equipment.getItemInMainHandDropChance();
                } catch (NoSuchMethodError ex) {
                    return equipment.getItemInHandDropChance();
                }
            case OFF_HAND:
                return equipment.getItemInOffHandDropChance();
            case FEET:
                return equipment.getBootsDropChance();
            case LEGS:
                return equipment.getLeggingsDropChance();
            case CHEST:
                return equipment.getChestplateDropChance();
            case HEAD:
                return equipment.getHelmetDropChance();
            default:
                return 0f;
        }
    }

    /**
     * 通过特定名称获取
     *
     * @param value 名称
     * @return {@link BukkitEquipment}
     */
    @Nullable
    public static BukkitEquipment fromString(String value) {
        switch (value.toLowerCase(Locale.getDefault())) {
            case "0":
            case "hand":
            case "mainhand":
                return HAND;
            case "1":
            case "head":
            case "helmet":
                return HEAD;
            case "2":
            case "chest":
            case "chestplate":
                return CHEST;
            case "3":
            case "legs":
            case "leggings":
                return LEGS;
            case "4":
            case "feet":
            case "boots":
                return FEET;
            case "-1":
            case "5":
            case "offhand":
                return OFF_HAND;
            default:
                return null;
        }
    }

    /**
     * 通过 nms 物品类型名称获取
     *
     * @param nms 名称
     * @return {@link BukkitEquipment}
     */
    @Nullable
    public static BukkitEquipment fromNMS(String nms) {
        return Arrays.stream(values()).filter(tEquipment -> tEquipment.nms.equalsIgnoreCase(nms)).findFirst().orElse(null);
    }

    /**
     * 通过 bukkit 物品类型名称获取
     *
     * @param bukkit 物品类型
     * @return {@link BukkitEquipment}
     */
    @Nullable
    public static BukkitEquipment fromBukkit(EquipmentSlot bukkit) {
        return Arrays.stream(values()).filter(tEquipment -> tEquipment.bukkit == bukkit).findFirst().orElse(null);
    }

    /**
     * 获取所有物品
     *
     * @param player 玩家实例
     * @return {@link Map} 位置对应的物品
     */
    @NotNull
    public static Map<BukkitEquipment, ItemStack> getItems(Player player) {
        return getItems((LivingEntity) player);
    }

    /**
     * 获取所有物品
     *
     * @param entity 实体实例
     * @return {@link Map} 位置对应的物品
     */
    @NotNull
    public static Map<BukkitEquipment, ItemStack> getItems(LivingEntity entity) {
        Map<BukkitEquipment, ItemStack> map = Maps.newHashMap();
        for (BukkitEquipment bukkitEquipment : values()) {
            ItemStack itemStack = bukkitEquipment.getItem(entity);
            if (itemStack != null) {
                map.put(bukkitEquipment, itemStack);
            }
        }
        return map;
    }

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
