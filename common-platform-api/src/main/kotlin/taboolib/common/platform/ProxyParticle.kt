package taboolib.common.platform

import taboolib.common.platform.function.onlinePlayers
import taboolib.common.util.Location
import taboolib.common.util.Vector
import java.awt.Color
import java.util.*

/**
 * <b>XParticle</b> - Particle enum for <b>XSeries</b>
 * <p>
 * This class is mainly used to support {@link Particle}, especially for the "parity change" by
 * Spigot in 1.20.5 (see <a href="https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/diff/src/main/java/org/bukkit/Particle.java?until=8a34e009148cc297bcc9eb5c250fc4f5b071c4a7">...</a>).
 *
 * @Author sky, Crypto Morin, Collin Barber
 * @Since 2020-08-04 18:36
 */
enum class ProxyParticle(vararg val aliases: String) {

    /**
     * EXPLOSION_NORMAL -> POOF (v1.20.5)
     */
    POOF("EXPLOSION_NORMAL"),

    /**
     * EXPLOSION_LARGE -> EXPLOSION (v1.20.5)
     */
    EXPLOSION("EXPLOSION_LARGE"),

    /**
     * EXPLOSION_HUGE -> EXPLOSION_EMITTER (v1.20.5)
     */
    EXPLOSION_EMITTER("EXPLOSION_HUGE"),

    /**
     * FIREWORKS_SPARK -> FIREWORK (v1.20.5)
     */
    FIREWORK("FIREWORKS_SPARK"),

    /**
     * WATER_BUBBLE -> BUBBLE (v1.20.5)
     */
    BUBBLE("WATER_BUBBLE"),

    /**
     * WATER_SPLASH -> SPLASH (v1.20.5)
     */
    SPLASH("WATER_SPLASH"),

    /**
     * WATER_WAKE -> FISHING (v1.20.5)
     */
    FISHING("WATER_WAKE"),

    /**
     * SUSPENDED -> UNDERWATER (v1.20.5)
     */
    UNDERWATER("SUSPENDED"),
    CRIT,

    /**
     * CRIT_MAGIC -> ENCHANTED_HIT (v1.20.5)
     */
    ENCHANTED_HIT("CRIT_MAGIC"),

    /**
     * SMOKE_NORMAL -> SMOKE (v1.20.5)
     */
    SMOKE("SMOKE_NORMAL"),

    /**
     * SMOKE_LARGE -> LARGE_SMOKE (v1.20.5)
     */
    LARGE_SMOKE("SMOKE_LARGE"),

    /**
     * SPELL -> EFFECT (v1.20.5)
     */
    EFFECT("SPELL"),

    /**
     * SPELL_INSTANT -> INSTANT_EFFECT (v1.20.5)
     */
    INSTANT_EFFECT("SPELL_INSTANT"),

    /**
     * SPELL_MOB_AMBIENT -> SPELL_MOB -> ENTITY_EFFECT (v1.20.5)
     * The name was changed multiple times during the parity update
     *
     * @see <a href="https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/diff/src/main/java/org/bukkit/Particle.java?until=1113e50a392b36253c4ae458a6d3d73e04841111">...</a>
     */
    ENTITY_EFFECT("SPELL_MOB", "SPELL_MOB_AMBIENT"),

    /**
     * SPELL_WITCH -> WITCH (v1.20.5)
     */
    WITCH("SPELL_WITCH"),

    /**
     * DRIP_WATER -> DRIPPING_WATER (v1.20.5)
     */
    DRIPPING_WATER("DRIP_WATER"),

    /**
     * DRIP_LAVA -> DRIPPING_LAVA (v1.20.5)
     */
    DRIPPING_LAVA("DRIP_LAVA"),

    /**
     * VILLAGER_ANGRY -> ANGRY_VILLAGER (v1.20.5)
     */
    ANGRY_VILLAGER("VILLAGER_ANGRY"),

    /**
     * VILLAGER_HAPPY -> HAPPY_VILLAGER (v1.20.5)
     */
    HAPPY_VILLAGER("VILLAGER_HAPPY"),

    /**
     * TOWN_AURA -> MYCELIUM (v1.20.5)
     */
    MYCELIUM("TOWN_AURA"),
    NOTE,
    PORTAL,

    /**
     * ENCHANTMENT_TABLE -> ENCHANT (v1.20.5)
     */
    ENCHANT("ENCHANTMENT_TABLE"),
    FLAME,
    LAVA,
    CLOUD,

    /**
     * REDSTONE -> DUST (v1.20.5)
     */
    DUST("REDSTONE"),

    /**
     * SNOWBALL, SNOW_SHOVEL -> ITEM_SNOWBALL (v1.20.5)
     */
    ITEM_SNOWBALL("SNOWBALL", "SNOW_SHOVEL"),

    /**
     * SLIME -> ITEM_SLIME (v1.20.5)
     */
    ITEM_SLIME("SLIME"),
    HEART,

    /**
     * ITEM_CRACK -> ITEM (v1.20.5)
     */
    ITEM("ITEM_CRACK"),

    /**
     * BLOCK_CRACK, BLOCK_DUST -> BLOCK (v1.20.5)
     */
    BLOCK("BLOCK_CRACK", "BLOCK_DUST"),

    /**
     * WATER_DROP -> RAIN (v1.20.5)
     */
    RAIN("WATER_DROP"),

    /**
     * MOB_APPEARANCE -> ELDER_GUARDIAN (v1.20.5)
     */
    ELDER_GUARDIAN("MOB_APPEARANCE"),
    DRAGON_BREATH,
    END_ROD,
    DAMAGE_INDICATOR,
    SWEEP_ATTACK,
    FALLING_DUST,

    /**
     * TOTEM -> TOTEM_OF_UNDYING (v1.20.5)
     */
    TOTEM_OF_UNDYING("TOTEM"),
    SPIT,
    SQUID_INK,
    BUBBLE_POP,
    CURRENT_DOWN,
    BUBBLE_COLUMN_UP,
    NAUTILUS,
    DOLPHIN,
    SNEEZE,
    CAMPFIRE_COSY_SMOKE,
    CAMPFIRE_SIGNAL_SMOKE,
    COMPOSTER,
    FLASH,
    FALLING_LAVA,
    LANDING_LAVA,
    FALLING_WATER,
    DRIPPING_HONEY,
    FALLING_HONEY,
    LANDING_HONEY,
    FALLING_NECTAR,
    SOUL_FIRE_FLAME,
    ASH,
    CRIMSON_SPORE,
    WARPED_SPORE,
    SOUL,
    DRIPPING_OBSIDIAN_TEAR,
    FALLING_OBSIDIAN_TEAR,
    LANDING_OBSIDIAN_TEAR,
    REVERSE_PORTAL,
    WHITE_ASH,
    DUST_COLOR_TRANSITION,
    VIBRATION,
    FALLING_SPORE_BLOSSOM,
    SPORE_BLOSSOM_AIR,
    SMALL_FLAME,
    SNOWFLAKE,
    DRIPPING_DRIPSTONE_LAVA,
    FALLING_DRIPSTONE_LAVA,
    DRIPPING_DRIPSTONE_WATER,
    FALLING_DRIPSTONE_WATER,
    GLOW_SQUID_INK,
    GLOW,
    WAX_ON,
    WAX_OFF,
    ELECTRIC_SPARK,
    SCRAPE,
    SONIC_BOOM,
    SCULK_SOUL,
    SCULK_CHARGE,
    SCULK_CHARGE_POP,
    SHRIEK,
    CHERRY_LEAVES,
    EGG_CRACK,
    DUST_PLUME,
    WHITE_SMOKE,
    GUST,
    SMALL_GUST,
    GUST_EMITTER_LARGE,
    GUST_EMITTER_SMALL,
    TRIAL_SPAWNER_DETECTION,
    TRIAL_SPAWNER_DETECTION_OMINOUS,
    VAULT_CONNECTION,
    INFESTED,
    ITEM_COBWEB,
    DUST_PILLAR,
    OMINOUS_SPAWNING,
    RAID_OMEN,
    TRIAL_OMEN,

    /**
     * BARRIER, LIGHT -> BLOCK_MARKER (v1.18)
     */
    BLOCK_MARKER("BARRIER", "LIGHT");

    /**
     * 将粒子发送给指定玩家
     */
    fun sendTo(player: ProxyPlayer, location: Location, offset: Vector = Vector(0, 0, 0), count: Int = 1, speed: Double = 0.0, data: Data? = null) {
        player.sendParticle(this, location, offset, count, speed, data)
    }

    interface Data

    open class DustData(val color: Color, val size: Float) : Data

    class DustTransitionData(color: Color, val toColor: Color, size: Float) : DustData(color, size)

    class ItemData(
        val material: String,
        val data: Int = 0,
        val name: String = "",
        val lore: List<String> = emptyList(),
        val customModelData: Int = -1,
    ) : Data

    class BlockData(val material: String, val data: Int = 0) : Data

    class VibrationData(val origin: Location, val destination: Destination, val arrivalTime: Int) : Data {

        sealed interface Destination

        class EntityDestination(val entity: UUID) : Destination

        class LocationDestination(val location: Location) : Destination
    }

    companion object {

        /**
         * 通过主名称或历史名（别名）检索 [ProxyParticle]
         */
        fun match(name: String, ignoreCase: Boolean = true): ProxyParticle? {
            return values().find { it.name.equals(name, ignoreCase) || it.aliases.any { alias -> alias.equals(name, ignoreCase) } }
        }
    }
}

/**
 * 将粒子发送到指定坐标
 */
fun ProxyParticle.sendTo(
    location: Location,
    range: Double = 128.0,
    offset: Vector = Vector(0, 0, 0),
    count: Int = 1,
    speed: Double = 0.0,
    data: ProxyParticle.Data? = null
) {
    onlinePlayers().filter { it.world == location.world && it.location.distance(location) <= range }.forEach {
        sendTo(it, location, offset, count, speed, data)
    }
}