package taboolib.module.navigation

import org.bukkit.block.Block

/**
 * Navigation
 * taboolib.module.navigation.Fluid
 *
 * @author sky
 * @since 2021/2/21 9:48 下午
 */
enum class Fluid {

    EMPTY, WATER, FLOWING_WATER, LAVA, FLOWING_LAVA;

    fun isLava() = this == LAVA || this == FLOWING_LAVA

    fun isWater() = this == WATER || this == FLOWING_WATER

    companion object {

        fun Block.getFluid() = when (type.name) {
            "LAVA" -> LAVA
            "STATIONARY_LAVA" -> FLOWING_LAVA
            "WATER" -> WATER
            "FLOWING_WATER" -> FLOWING_WATER
            else -> EMPTY
        }

        fun String.getFluid() = when (this) {
            "LAVA" -> LAVA
            "STATIONARY_LAVA" -> FLOWING_LAVA
            "WATER" -> WATER
            "FLOWING_WATER" -> FLOWING_WATER
            else -> EMPTY
        }
    }
}