package taboolib.module.multiblocks

/**
 * 简单的整数坐标，用于表示多方块结构中的相对位置
 *
 * @author FxRayHughes
 * @since 2026/3/30
 */
data class BlockPos(val x: Int, val y: Int, val z: Int) {

    operator fun plus(other: BlockPos) = BlockPos(x + other.x, y + other.y, z + other.z)

    operator fun minus(other: BlockPos) = BlockPos(x - other.x, y - other.y, z - other.z)

    /**
     * 绕 Y 轴旋转该坐标
     */
    fun rotate(rotation: MultiblockRotation): BlockPos {
        return when (rotation) {
            MultiblockRotation.NONE -> this
            MultiblockRotation.CLOCKWISE_90 -> BlockPos(-z, y, x)
            MultiblockRotation.CLOCKWISE_180 -> BlockPos(-x, y, -z)
            MultiblockRotation.COUNTERCLOCKWISE_90 -> BlockPos(z, y, -x)
        }
    }

    companion object {

        val ZERO = BlockPos(0, 0, 0)
    }
}
