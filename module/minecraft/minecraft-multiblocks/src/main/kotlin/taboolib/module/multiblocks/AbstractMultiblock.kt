package taboolib.module.multiblocks

import org.bukkit.World

/**
 * 多方块结构的抽象基类
 *
 * 实现通用的验证逻辑和旋转检测。
 * 子类只需实现 [simulate] 和 [size] 即可。
 *
 * @author FxRayHughes
 * @since 2026/3/30
 */
abstract class AbstractMultiblock : IMultiblock {

    override var id: String = ""
    override var symmetrical: Boolean = false

    /** '0' 锚点在图案中的坐标 */
    protected var offX: Int = 0
    protected var offY: Int = 0
    protected var offZ: Int = 0

    override fun offset(x: Int, y: Int, z: Int): IMultiblock {
        offX = x
        offY = y
        offZ = z
        return this
    }

    override fun validate(world: World, anchor: BlockPos): MultiblockRotation? {
        val rotations = if (symmetrical) listOf(MultiblockRotation.NONE) else MultiblockRotation.ALL
        for (rotation in rotations) {
            if (validate(world, anchor, rotation)) {
                return rotation
            }
        }
        return null
    }

    override fun validate(world: World, anchor: BlockPos, rotation: MultiblockRotation): Boolean {
        val results = simulate(anchor, rotation)
        for (result in results) {
            val pos = result.worldPosition
            val block = world.getBlockAt(pos.x, pos.y, pos.z)
            if (!result.stateMatcher.test(block)) {
                return false
            }
        }
        return true
    }

    override fun test(world: World, anchor: BlockPos, x: Int, y: Int, z: Int, rotation: MultiblockRotation): Boolean {
        val relativePos = BlockPos(x - offX, y - offY, z - offZ)
        val rotatedPos = relativePos.rotate(rotation)
        val worldPos = anchor + rotatedPos
        val results = simulate(anchor, rotation)
        val result = results.find { it.worldPosition == worldPos } ?: return true
        val block = world.getBlockAt(worldPos.x, worldPos.y, worldPos.z)
        return result.stateMatcher.test(block)
    }
}
