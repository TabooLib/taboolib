package taboolib.module.multiblocks

/**
 * 稀疏型多方块结构
 *
 * 使用位置到匹配器的映射定义结构，适用于方块分布稀疏的大型结构。
 * 所有位置均相对于锚点（0, 0, 0）。
 *
 * ## 示例
 *
 * ```kotlin
 * val multiblock = SparseMultiblock(
 *     blocks = mapOf(
 *         BlockPos(1, 0, 0) to StringStateMatcher.parse("minecraft:stone"),
 *         BlockPos(-1, 0, 0) to StringStateMatcher.parse("minecraft:stone"),
 *         BlockPos(0, 0, 1) to StringStateMatcher.parse("minecraft:stone"),
 *         BlockPos(0, 0, -1) to StringStateMatcher.parse("minecraft:stone"),
 *         BlockPos(0, 1, 0) to StringStateMatcher.parse("minecraft:stone"),
 *     )
 * )
 * ```
 *
 * @param blocks 相对位置到状态匹配器的映射，位置相对于锚点
 *
 * @author FxRayHughes
 * @since 2026/3/30
 */
class SparseMultiblock(
    private val blocks: Map<BlockPos, IStateMatcher>
) : AbstractMultiblock() {

    override val size: BlockPos

    init {
        // 计算包围盒
        if (blocks.isEmpty()) {
            size = BlockPos.ZERO
        } else {
            var minX = Int.MAX_VALUE
            var minY = Int.MAX_VALUE
            var minZ = Int.MAX_VALUE
            var maxX = Int.MIN_VALUE
            var maxY = Int.MIN_VALUE
            var maxZ = Int.MIN_VALUE
            for (pos in blocks.keys) {
                if (pos.x < minX) minX = pos.x
                if (pos.y < minY) minY = pos.y
                if (pos.z < minZ) minZ = pos.z
                if (pos.x > maxX) maxX = pos.x
                if (pos.y > maxY) maxY = pos.y
                if (pos.z > maxZ) maxZ = pos.z
            }
            size = BlockPos(maxX - minX + 1, maxY - minY + 1, maxZ - minZ + 1)
        }
        // 稀疏型结构默认锚点在原点
        offX = 0
        offY = 0
        offZ = 0
    }

    override fun simulate(anchor: BlockPos, rotation: MultiblockRotation): List<SimulateResult> {
        return blocks.map { (relativePos, matcher) ->
            val rotatedPos = relativePos.rotate(rotation)
            val worldPos = anchor + rotatedPos
            SimulateResult(worldPos, matcher)
        }
    }
}
