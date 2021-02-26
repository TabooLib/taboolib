package io.izzel.taboolib.module.command.base;

import com.mojang.brigadier.arguments.*;
import io.izzel.taboolib.kotlin.Reflex;
import io.izzel.taboolib.module.nms.NMS;

/**
 * TabooLib
 * io.izzel.taboolib.module.command.base.ArgumentType
 *
 * @author bkm016
 * @since 2020/10/20 11:32 下午
 */
public abstract class ArgumentType {

    abstract public Object toNMS();

    public static ArgumentType bool() {
        return new ArgumentType() {

            @Override
            public Object toNMS() {
                return BoolArgumentType.bool();
            }
        };
    }

    public static ArgumentType word() {
        return new ArgumentType() {

            @Override
            public Object toNMS() {
                return StringArgumentType.word();
            }
        };
    }

    public static ArgumentType string() {
        return new ArgumentType() {

            @Override
            public Object toNMS() {
                return StringArgumentType.string();
            }
        };
    }

    public static ArgumentType greedyString() {
        return new ArgumentType() {

            @Override
            public Object toNMS() {
                return StringArgumentType.greedyString();
            }
        };
    }

    public static ArgumentType doubleArg() {
        return doubleArg(Double.MIN_VALUE, Double.MAX_VALUE);
    }

    public static ArgumentType doubleArg(double min) {
        return doubleArg(min, Double.MAX_VALUE);
    }

    public static ArgumentType doubleArg(double min, double max) {
        return new ArgumentType() {

            @Override
            public Object toNMS() {
                return DoubleArgumentType.doubleArg(min, max);
            }
        };
    }

    public static ArgumentType floatArg() {
        return floatArg(Float.MIN_VALUE, Float.MAX_VALUE);
    }

    public static ArgumentType floatArg(float min) {
        return floatArg(min, Float.MAX_VALUE);
    }

    public static ArgumentType floatArg(float min, float max) {
        return new ArgumentType() {

            @Override
            public Object toNMS() {
                return FloatArgumentType.floatArg(min, max);
            }
        };
    }

    public static ArgumentType longArg() {
        return longArg(Long.MIN_VALUE, Long.MAX_VALUE);
    }

    public static ArgumentType longArg(long min) {
        return longArg(min, Long.MAX_VALUE);
    }

    public static ArgumentType longArg(long min, long max) {
        return new ArgumentType() {

            @Override
            public Object toNMS() {
                return ArgumentType.longArg(min, max);
            }
        };
    }

    public static ArgumentType integer() {
        return integer(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public static ArgumentType integer(int min) {
        return integer(min, Integer.MAX_VALUE);
    }

    public static ArgumentType integer(int min, int max) {
        return new ArgumentType() {

            @Override
            public Object toNMS() {
                return IntegerArgumentType.integer(min, max);
            }
        };
    }

    public static ArgumentType bukkit(Bukkit bukkit) {
        return new ArgumentType() {

            @Override
            public Object toNMS() {
                return Reflex.Companion.from(NMS.handle().asNMS(bukkit.getKey())).invoke("a");
            }
        };
    }

    public enum Bukkit {

        ANCHOR("ArgumentAnchor"),

        BLOCK_PREDICATE("ArgumentBlockPredicate"),

        CHAT("ArgumentChat"),

        CHAT_COMPONENT("ArgumentChatComponent"),

        CHAT_FORMAT("ArgumentChatFormat"),

        CRITERION_VALUE("ArgumentCriterionValue"),

        DIMENSION("ArgumentDimension"),

        ENCHANTMENT("ArgumentEnchantment"),

        ENTITY("ArgumentEntity"),

        ENTITY_SUMMON("ArgumentEntitySummon"),

        INVENTORY_SLOT("ArgumentInventorySlot"),

        ITEM_PREDICATE("ArgumentItemPredicate"),

        ITEM_STACK("ArgumentItemStack"),

        MATH_OPERATION("ArgumentMathOperation"),

        MINECRAFT_KEY_REGISTERED("ArgumentMinecraftKeyRegistered"),

        MOB_EFFECT("ArgumentMobEffect"),

        NBT_BASE("ArgumentNBTBase"),

        NBT_KEY("ArgumentNBTKey"),

        NBT_TAG("ArgumentNBTTag"),

        PARTICLE("ArgumentParticle"),

        POSITION("ArgumentPosition"),

        PROFILE("ArgumentProfile"),

        ROTATION("ArgumentRotation"),

        ROTATION_AXIS("ArgumentRotationAxis"),

        SCOREBOARD_CRITERIA("ArgumentScoreboardCriteria"),

        SCOREBOARD_OBJECTIVE("ArgumentScoreboardObjective"),

        SCOREBOARD_SLOT("ArgumentScoreboardSlot"),

        SCOREBOARD_TEAM("ArgumentScoreboardTeam"),

        SCOREHOLDER("ArgumentScoreholder"),

        TAG("ArgumentTag"),

        TILE("ArgumentTile"),

        TILE_LOCATION("ArgumentTileLocation"),

        TIME("ArgumentTime"),

        UUID("ArgumentUUID"),

        VEC2("ArgumentVec2"),

        VEC2I("ArgumentVec2I"),

        VEC3("ArgumentVec3"),

        ;

        String key;

        Bukkit(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }
}
