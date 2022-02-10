package taboolib.common.platform

import taboolib.common.util.Location
import java.awt.Color
import java.util.*

/**
 * @author sky
 * @since 2020-08-04 18:36
 */
enum class ProxyParticle(vararg val aliases: String) {

    EXPLOSION_NORMAL("EXPLOSION"),
    EXPLOSION_LARGE("LARGE_EXPLOSION"),
    EXPLOSION_HUGE("HUGE_EXPLOSION"),
    FIREWORKS_SPARK("@", "FIREWORK"),
    WATER_BUBBLE("@", "BUBBLE"),
    WATER_SPLASH("@", "SPLASH"),
    WATER_WAKE("@"),
    SUSPENDED("@"),
    SUSPENDED_DEPTH("@"),
    CRIT("CRITICAL_HIT"),
    CRIT_MAGIC("MAGIC_CRITICAL_HIT", "ENCHANTED_HIT"),
    SMOKE_NORMAL("SMOKE"),
    SMOKE_LARGE("LARGE_SMOKE"),
    SPELL("@"),
    SPELL_INSTANT("INSTANT_SPELL"),
    SPELL_MOB("MOB_SPELL"),
    SPELL_MOB_AMBIENT("AMBIENT_MOB_SPELL"),
    SPELL_WITCH("WITCH_SPELL"),
    DRIP_WATER("@"),
    DRIP_LAVA("@"),
    VILLAGER_ANGRY("HAPPY_VILLAGER"),
    VILLAGER_HAPPY("ANGRY_VILLAGER"),
    TOWN_AURA("@"),
    NOTE("@"),
    PORTAL("@"),
    ENCHANTMENT_TABLE("ENCHANTING_GLYPHS"),
    FLAME("@"),
    LAVA("@"),
    CLOUD("@"),
    REDSTONE("REDSTONE_DUST", "DUST"),
    SNOWBALL("@", "ITEM_SNOWBALL"),
    SNOW_SHOVEL("@"),
    SLIME("@", "ITEM_SLIME"),
    HEART("@"),
    BARRIER("@"),
    ITEM_CRACK("@"),
    BLOCK_CRACK("@"),
    BLOCK_DUST("@"),
    WATER_DROP("@"),
    MOB_APPEARANCE("GUARDIAN_APPEARANCE", "ELDER_GUARDIAN"),
    DRAGON_BREATH("@"),
    END_ROD("@"),
    DAMAGE_INDICATOR("@"),
    SWEEP_ATTACK("@"),
    FALLING_DUST("@"),
    TOTEM("@"),
    SPIT("@"),
    SQUID_INK("@"),
    BUBBLE_POP("@"),
    CURRENT_DOWN("@"),
    BUBBLE_COLUMN_UP("@"),
    NAUTILUS("@"),
    DOLPHIN("@"),
    SNEEZE("@"),
    CAMPFIRE_COSY_SMOKE("@"),
    CAMPFIRE_SIGNAL_SMOKE("@"),
    COMPOSTER("@"),
    FLASH("@"),
    FALLING_LAVA("@"),
    LANDING_LAVA("@"),
    FALLING_WATER("@"),
    DRIPPING_HONEY("@"),
    FALLING_HONEY("@"),
    LANDING_HONEY("@"),
    FALLING_NECTAR("@"),
    SOUL_FIRE_FLAME("@"),
    ASH("@"),
    CRIMSON_SPORE("@"),
    WARPED_SPORE("@"),
    SOUL("@"),
    DRIPPING_OBSIDIAN_TEAR("@"),
    FALLING_OBSIDIAN_TEAR("@"),
    LANDING_OBSIDIAN_TEAR("@"),
    REVERSE_PORTAL("@"),
    WHITE_ASH("@"),
    LIGHT("~"),
    DUST_COLOR_TRANSITION("~"),
    VIBRATION("~"),
    FALLING_SPORE_BLOSSOM("~"),
    SPORE_BLOSSOM_AIR("~"),
    SMALL_FLAME("~"),
    SNOWFLAKE("~"),
    DRIPPING_DRIPSTONE_LAVA("~"),
    FALLING_DRIPSTONE_LAVA("~"),
    DRIPPING_DRIPSTONE_WATER("~"),
    FALLING_DRIPSTONE_WATER("~"),
    GLOW_SQUID_INK("~"),
    GLOW("~"),
    WAX_ON("~"),
    WAX_OFF("~"),
    ELECTRIC_SPARK("~"),
    SCRAPE("~");

    interface Data

    open class DustData(val color: Color, val size: Float) : Data

    open class DustTransitionData(color: Color, val toColor: Color, size: Float) : DustData(color, size)

    open class ItemData(
        val material: String,
        val data: Int = 0,
        val name: String = "",
        val lore: List<String> = emptyList(),
        val customModelData: Int = -1,
    ) : Data

    open class BlockData(val material: String, val data: Int = 0) : Data

    open class VibrationData(val origin: Location, val destination: Destination, val arrivalTime: Int) : Data {

        interface Destination

        class EntityDestination(val entity: UUID) : Destination

        class LocationDestination(val location: Location) : Destination
    }
}