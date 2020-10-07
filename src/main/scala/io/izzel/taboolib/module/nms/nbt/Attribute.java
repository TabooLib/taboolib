package io.izzel.taboolib.module.nms.nbt;

import io.izzel.taboolib.module.nms.NMS;

import java.util.Arrays;

/**
 * 物品属性映射类
 *
 * @Author sky
 * @Since 2019-12-11 19:31
 */
public enum Attribute {

    MAX_HEALTH("generic.maxHealth", new String[]{"health", "maxHealth"}),

    FOLLOW_RANGE("generic.followRange", new String[]{"follow", "followRange"}),

    KNOCKBACK_RESISTANCE("generic.knockbackResistance", new String[]{"knockback", "knockbackResistance"}),

    MOVEMENT_SPEED("generic.movementSpeed", new String[]{"speed", "movementSpeed", "walkSpeed"}),

    FLYING_SPEED("generic.flyingSpeed", new String[]{"flySpeed", "flyingSpeed"}),

    ATTACK_DAMAGE("generic.attackDamage", new String[]{"damage", "attackDamage"}),

    ATTACK_KNOCKBACK("generic.attackKnockback", new String[]{"damageKnockback", "attackKnockback"}),

    ATTACK_SPEED("generic.attackSpeed", new String[]{"damageSpeed", "attackSpeed"}),

    ARMOR("generic.armor", new String[]{"armor"}),

    ARMOR_TOUGHNESS("generic.armorToughness", new String[]{"toughness", "armorToughness"}),

    LUCK("generic.luck", new String[]{"luck"});

    String minecraftKey;
    String[] simplifiedKey;

    Attribute(String minecraftKey, String[] simplifiedKey) {
        this.minecraftKey = minecraftKey;
        this.simplifiedKey = simplifiedKey;
    }

    public String getMinecraftKey() {
        return minecraftKey;
    }

    public String[] getSimplifiedKey() {
        return simplifiedKey;
    }

    public Object toNMS() {
        return NMS.handle().toNMS(this);
    }

    public boolean match(String source) {
        return name().equalsIgnoreCase(source) || minecraftKey.equalsIgnoreCase(source) || Arrays.stream(simplifiedKey).anyMatch(key -> key.equalsIgnoreCase(source));
    }

    public static Attribute parse(String source) {
        return Arrays.stream(values()).filter(attribute -> attribute.match(source)).findFirst().orElse(null);
    }
}
