package taboolib.module.multiblocks

/**
 * 密集型多方块结构
 *
 * 使用二维字符串数组定义结构图案，每个字符映射到一个 [IStateMatcher]。
 * 适用于紧凑的、方块密度较高的结构。
 *
 * ## 图案格式
 *
 * ```kotlin
 * val multiblock = DenseMultiblock(
 *     pattern = arrayOf(
 *         // 第一个数组 = 最顶层（最高 Y）
 *         arrayOf(
 *             "   ",
 *             " S ",
 *             "   "
 *         ),
 *         // 最后一个数组 = 最底层（最低 Y）
 *         arrayOf(
 *             "SSS",
 *             "S0S",
 *             "SSS"
 *         )
 *     ),
 *     mapping = mapOf(
 *         'S' to StringStateMatcher.parse("minecraft:stone")
 *     )
 * )
 * ```
 *
 * ## 特殊字符
 * - `0` — 中心锚点（默认映射为空气，可在 mapping 中覆盖）
 * - `_` — 匹配任意方块
 * - ` ` (空格) — 空气
 *
 * @param pattern 结构图案，外层数组为 Y 层（从上到下），内层数组为 Z 行，字符串中的字符为 X 列
 * @param mapping 字符到状态匹配器的映射
 *
 * @author FxRayHughes
 * @since 2026/3/30
 */
class DenseMultiblock(
    pattern: Array<Array<String>>,
    mapping: Map<Char, IStateMatcher> = emptyMap()
) : AbstractMultiblock() {

    private val stateTargets: Array<Array<Array<IStateMatcher>>>
    private val charMap: Array<Array<CharArray>>
    override val size: BlockPos

    init {
        // 合并默认映射
        val fullMapping = HashMap<Char, IStateMatcher>().apply {
            put('_', StateMatcher.ANY)
            put(' ', StateMatcher.AIR)
            put('0', StateMatcher.AIR)
            putAll(mapping)
        }

        // 解析图案尺寸
        val sizeY = pattern.size
        val sizeZ = if (pattern.isNotEmpty()) pattern[0].size else 0
        val sizeX = if (sizeZ > 0) pattern[0][0].length else 0
        size = BlockPos(sizeX, sizeY, sizeZ)

        // 构建状态匹配器和字符的三维数组
        // pattern[0] = 最顶层, pattern[last] = 最底层
        // 实际 Y 坐标: actualY = sizeY - 1 - patternIndex
        stateTargets = Array(sizeY) { actualY ->
            val patternIndex = sizeY - 1 - actualY
            val layer = pattern.getOrNull(patternIndex) ?: emptyArray()
            Array(sizeZ) { z ->
                val row = layer.getOrNull(z) ?: ""
                Array(sizeX) { x ->
                    val ch = row.getOrNull(x) ?: ' '
                    fullMapping[ch] ?: StateMatcher.AIR
                }
            }
        }
        charMap = Array(sizeY) { actualY ->
            val patternIndex = sizeY - 1 - actualY
            val layer = pattern.getOrNull(patternIndex) ?: emptyArray()
            Array(sizeZ) { z ->
                val row = layer.getOrNull(z) ?: ""
                CharArray(sizeX) { x -> row.getOrNull(x) ?: ' ' }
            }
        }

        // 查找中心锚点 '0' 并设置偏移
        var found = false
        for (patternIndex in pattern.indices) {
            val actualY = sizeY - 1 - patternIndex
            val layer = pattern[patternIndex]
            for (z in layer.indices) {
                val row = layer[z]
                for (x in row.indices) {
                    if (row[x] == '0') {
                        offX = x
                        offY = actualY
                        offZ = z
                        found = true
                    }
                }
            }
        }
        if (!found) {
            offX = 0
            offY = 0
            offZ = 0
        }
    }

    override fun simulate(anchor: BlockPos, rotation: MultiblockRotation): List<SimulateResult> {
        val results = mutableListOf<SimulateResult>()
        for (y in 0 until size.y) {
            for (z in 0 until size.z) {
                for (x in 0 until size.x) {
                    val matcher = stateTargets[y][z][x]
                    // 跳过 ANY 匹配器（任意方块都满足，无需检测）
                    if (matcher === StateMatcher.ANY) continue
                    val ch = charMap[y][z][x]
                    // 计算相对于锚点的位置
                    val relativePos = BlockPos(x - offX, y - offY, z - offZ)
                    val rotatedPos = relativePos.rotate(rotation)
                    val worldPos = anchor + rotatedPos
                    results.add(SimulateResult(worldPos, matcher, ch))
                }
            }
        }
        return results
    }
}
