package taboolib.platform.util;

import org.bukkit.Material;
import taboolib.common.Isolated;

import java.util.HashMap;
import java.util.Map;

/**
 * 原版装备默认属性表
 * 接受 item.getType(), 返回属性小数值
 * 返回 0.0 则物品没有属性
 *
 * @author YiMiner
 * @version 1.1
 * Jun 25, 2021
 */
@Isolated
public class DefaultAttribute {

    private static final HashMap<String, Double> ATTACK_SPEED = new HashMap<>();
    private static final HashMap<String, Double> ATTACK_DAMAGE = new HashMap<>();
    private static final HashMap<String, Double> ARMOR = new HashMap<>();
    private static final HashMap<String, Double> ARMOR_TOUGHNESS = new HashMap<>();
    private static final HashMap<String, Double> KNOCKBACK_RESISTANCE = new HashMap<>();

    static {
        // 攻击速度
        ATTACK_SPEED.put("SWORD", 1.6);
        ATTACK_SPEED.put("TRIDENT", 1.1);
        ATTACK_SPEED.put("SHOVEL", 1.0);
        ATTACK_SPEED.put("PICKAXE", 1.2);
        // 上述四类单独检索
        ATTACK_SPEED.put("WOODEN_AXE", 0.8);
        ATTACK_SPEED.put("GOLDEN_AXE", 1.0);
        ATTACK_SPEED.put("STONE_AXE", 0.8);
        ATTACK_SPEED.put("IRON_AXE", 0.9);
        ATTACK_SPEED.put("DIAMOND_AXE", 1.0);
        ATTACK_SPEED.put("NETHERITE_AXE", 1.0);
        ATTACK_SPEED.put("WOODEN_HOE", 1.0);
        ATTACK_SPEED.put("GOLDEN_HOE", 1.0);
        ATTACK_SPEED.put("STONE_HOE", 2.0);
        ATTACK_SPEED.put("IRON_HOE", 3.0);
        ATTACK_SPEED.put("DIAMOND_HOE", 4.0);
        ATTACK_SPEED.put("NETHERITE_HOE", 4.0);
        // 攻击伤害
        // 锄头单独处理
        ATTACK_DAMAGE.put("HOE", 1.0);
        ATTACK_DAMAGE.put("TRIDENT", 9.0);
        ATTACK_DAMAGE.put("WOODEN_SWORD", 4.0);
        ATTACK_DAMAGE.put("GOLDEN_SWORD", 4.0);
        ATTACK_DAMAGE.put("STONE_SWORD", 5.0);
        ATTACK_DAMAGE.put("IRON_SWORD", 6.0);
        ATTACK_DAMAGE.put("DIAMOND_SWORD", 7.0);
        ATTACK_DAMAGE.put("NETHERITE_SWORD", 8.0);
        // 铲子
        ATTACK_DAMAGE.put("WOODEN_SHOVEL", 2.5);
        ATTACK_DAMAGE.put("GOLDEN_SHOVEL", 2.5);
        ATTACK_DAMAGE.put("STONE_SHOVEL", 3.5);
        ATTACK_DAMAGE.put("IRON_SHOVEL", 4.5);
        ATTACK_DAMAGE.put("DIAMOND_SHOVEL", 5.5);
        ATTACK_DAMAGE.put("NETHERITE_SHOVEL", 6.5);
        // 稿子
        ATTACK_DAMAGE.put("WOODEN_PICKAXE", 2.0);
        ATTACK_DAMAGE.put("GOLDEN_PICKAXE", 2.0);
        ATTACK_DAMAGE.put("STONE_PICKAXE", 3.0);
        ATTACK_DAMAGE.put("IRON_PICKAXE", 4.0);
        ATTACK_DAMAGE.put("DIAMOND_PICKAXE", 5.0);
        ATTACK_DAMAGE.put("NETHERITE_PICKAXE", 6.0);
        // 斧子
        ATTACK_DAMAGE.put("WOODEN_AXE", 7.0);
        ATTACK_DAMAGE.put("GOLDEN_AXE", 7.0);
        ATTACK_DAMAGE.put("STONE_AXE", 9.0);
        ATTACK_DAMAGE.put("IRON_AXE", 9.0);
        ATTACK_DAMAGE.put("DIAMOND_AXE", 9.0);
        ATTACK_DAMAGE.put("NETHERITE_AXE", 10.0);
        // 盔甲值
        ARMOR.put("TURTLE_HELMET", 2.0);
        // 皮
        ARMOR.put("LEATHER_HELMET", 1.0);
        ARMOR.put("LEATHER_CHESTPLATE", 3.0);
        ARMOR.put("LEATHER_LEGGINGS", 2.0);
        ARMOR.put("LEATHER_BOOTS", 1.0);
        // 金
        ARMOR.put("GOLDEN_HELMET", 2.0);
        ARMOR.put("GOLDEN_CHESTPLATE", 5.0);
        ARMOR.put("GOLDEN_LEGGINGS", 3.0);
        ARMOR.put("GOLDEN_BOOTS", 1.0);
        // 锁链
        ARMOR.put("CHAINMAIL_HELMET", 2.0);
        ARMOR.put("CHAINMAIL_CHESTPLATE", 5.0);
        ARMOR.put("CHAINMAIL_LEGGINGS", 4.0);
        ARMOR.put("CHAINMAIL_BOOTS", 1.0);
        // 铁
        ARMOR.put("IRON_HELMET", 2.0);
        ARMOR.put("IRON_CHESTPLATE", 6.0);
        ARMOR.put("IRON_LEGGINGS", 5.0);
        ARMOR.put("IRON_BOOTS", 2.0);
        // 钻
        ARMOR.put("DIAMOND_HELMET", 3.0);
        ARMOR.put("DIAMOND_CHESTPLATE", 8.0);
        ARMOR.put("DIAMOND_LEGGINGS", 6.0);
        ARMOR.put("DIAMOND_BOOTS", 3.0);
        // 合金
        ARMOR.put("NETHERITE_HELMET", 3.0);
        ARMOR.put("NETHERITE_CHESTPLATE", 8.0);
        ARMOR.put("NETHERITE_LEGGINGS", 6.0);
        ARMOR.put("NETHERITE_BOOTS", 3.0);
        // 盔甲韧性
        // 钻
        ARMOR_TOUGHNESS.put("DIAMOND_HELMET", 2.0);
        ARMOR_TOUGHNESS.put("DIAMOND_CHESTPLATE", 2.0);
        ARMOR_TOUGHNESS.put("DIAMOND_LEGGINGS", 2.0);
        ARMOR_TOUGHNESS.put("DIAMOND_BOOTS", 2.0);
        // 合金
        ARMOR_TOUGHNESS.put("NETHERITE_HELMET", 3.0);
        ARMOR_TOUGHNESS.put("NETHERITE_CHESTPLATE", 3.0);
        ARMOR_TOUGHNESS.put("NETHERITE_LEGGINGS", 3.0);
        ARMOR_TOUGHNESS.put("NETHERITE_BOOTS", 3.0);
        // 击退抗性
        // 合金
        KNOCKBACK_RESISTANCE.put("NETHERITE_HELMET", 0.1);
        KNOCKBACK_RESISTANCE.put("NETHERITE_CHESTPLATE", 0.1);
        KNOCKBACK_RESISTANCE.put("NETHERITE_LEGGINGS", 0.1);
        KNOCKBACK_RESISTANCE.put("NETHERITE_BOOTS", 0.1);
    }

    public static double getAttackDamage(Material type) {
        if (type.name().endsWith("_HOE")) {
            return ATTACK_DAMAGE.get("HOE");
        } else {
            return ATTACK_DAMAGE.getOrDefault(type.name(), 0.0);
        }
    }

    public static double getAttackSpeed(Material type) {
        if (type.name().endsWith("_SWORD")) {
            return ATTACK_SPEED.get("SWORD");
        } else if (type.name().endsWith("_SHOVEL")) {
            return ATTACK_SPEED.get("SHOVEL");
        } else if (type.name().endsWith("_PICKAXE")) {
            return ATTACK_SPEED.get("PICKAXE");
        } else {
            return ATTACK_SPEED.getOrDefault(type.name(), 0.0);
        }
    }

    public static double getArmor(Material type) {
        return ARMOR.getOrDefault(type.name(), 0.0);
    }

    public static double getArmorToughness(Material type) {
        return ARMOR_TOUGHNESS.getOrDefault(type.name(), 0.0);
    }

    public static double getKnockbackResistance(Material type) {
        return KNOCKBACK_RESISTANCE.getOrDefault(type.name(), 0.0);
    }

    public static Map<String, Double> getDefault(Material type) {
        Map<String, Double> map = new HashMap<>();
        double attackDamage = getAttackDamage(type);
        if (attackDamage > 0) {
            map.put("GENERIC_ATTACK_DAMAGE", attackDamage);
        }
        double attackSpeed = getAttackSpeed(type);
        if (attackSpeed > 0) {
            map.put("GENERIC_ATTACK_SPEED", attackSpeed);
        }
        double armor = getArmor(type);
        if (armor > 0) {
            map.put("GENERIC_ARMOR", armor);
        }
        double armorToughness = getArmorToughness(type);
        if (armorToughness > 0) {
            map.put("GENERIC_ARMOR_TOUGHNESS", armorToughness);
        }
        double knockbackResistance = getKnockbackResistance(type);
        if (knockbackResistance > 0) {
            map.put("GENERIC_KNOCKBACK_RESISTANCE", knockbackResistance);
        }
        return map;
    }
}
