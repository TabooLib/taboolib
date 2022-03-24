package taboolib.platform.util

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Entity

/**
 * 获取生物脚下的方块.
 */
val Entity.groundBlock: Block
    get() = location.add(0.0, -0.01, 0.0).block

/**
 * 获取生物脚下方块的材质.
 */
val Entity.groundBlockType: Material
    get() = groundBlock.type