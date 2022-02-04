package taboolib.module.navigation

import org.bukkit.block.Block
import org.bukkit.entity.Entity
import taboolib.module.nms.nmsProxy

/**
 * Navigation
 * taboolib.module.navigation.NMS
 *
 * @author sky
 * @since 2021/2/21 11:57 下午
 */
abstract class NMS {

    abstract fun getBoundingBox(entity: Entity): BoundingBox?

    abstract fun getBoundingBox(block: Block): BoundingBox?

    abstract fun getBlockHeight(block: Block): Double

    abstract fun isDoorOpened(block: Block): Boolean

    companion object {

        val INSTANCE = nmsProxy(NMS::class.java)
    }
}