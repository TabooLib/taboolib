package taboolib.module.navigation

import org.bukkit.block.Block
import org.bukkit.block.data.Waterlogged
import taboolib.module.nms.MinecraftVersion

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
            "STATIONARY_LAVA" -> LAVA
            "FLOWING_LAVA" -> FLOWING_LAVA
            "WATER" -> WATER
            "STATIONARY_WATER" -> WATER
            "FLOWING_WATER" -> FLOWING_WATER
            else -> {
                // Bukkit 的 getBlockData() 每次调用都会新建 BlockData 对象，
                // 而本方法处在寻路的热点路径上（getStartAtRegion 的纵向扫描会反复调用）。
                // 空气占绝大多数且不可能含水，先做一次零分配的短路判断。
                // 更上层的缓存见 NodeReader.getCachedFluid。
                if (!type.isAirLegacy() && MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_13)) {
                    (blockData as? Waterlogged)?.takeIf { it.isWaterlogged }?.let { WATER } ?: EMPTY
                } else {
                    EMPTY
                }
            }
        }

        fun String.getFluid() = when (this) {
            "LAVA" -> LAVA
            "STATIONARY_LAVA" -> LAVA
            "FLOWING_LAVA" -> FLOWING_LAVA
            "WATER" -> WATER
            "STATIONARY_WATER" -> WATER
            "FLOWING_WATER" -> FLOWING_WATER
            else -> EMPTY
        }
    }
}