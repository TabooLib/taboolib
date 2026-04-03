package taboolib.module.multiblocks

import org.bukkit.World

/**
 * 多方块结构接口
 *
 * 定义了多方块的核心行为：验证、模拟、放置等。
 * 参考 Patchouli 的 IMultiblock 设计，适配 TabooLib/Bukkit 生态。
 *
 * @author FxRayHughes
 * @since 2026/3/30
 */
interface IMultiblock {

    /** 结构标识符 */
    var id: String

    /** 是否对称（对称结构只检查一个旋转方向，提升性能） */
    var symmetrical: Boolean

    /** 结构的尺寸 */
    val size: BlockPos

    /**
     * 设置结构相对于锚点的偏移
     */
    fun offset(x: Int, y: Int, z: Int): IMultiblock

    /**
     * 模拟结构，返回所有组成方块的世界坐标和匹配器
     *
     * @param anchor 锚点（'0' 标记位置）在世界中的坐标
     * @param rotation 旋转方向
     * @return 结构中每个方块的模拟结果
     */
    fun simulate(anchor: BlockPos, rotation: MultiblockRotation): List<SimulateResult>

    /**
     * 验证世界中指定位置是否存在该结构（自动尝试所有旋转方向）
     *
     * @param world 世界
     * @param anchor 锚点坐标
     * @return 匹配的旋转方向，若不匹配返回 null
     */
    fun validate(world: World, anchor: BlockPos): MultiblockRotation?

    /**
     * 验证世界中指定位置、指定旋转方向是否存在该结构
     *
     * @param world 世界
     * @param anchor 锚点坐标
     * @param rotation 旋转方向
     * @return 是否匹配
     */
    fun validate(world: World, anchor: BlockPos, rotation: MultiblockRotation): Boolean

    /**
     * 测试结构中某个相对位置的方块是否匹配
     *
     * @param world 世界
     * @param anchor 锚点坐标
     * @param x 结构内 X 坐标
     * @param y 结构内 Y 坐标
     * @param z 结构内 Z 坐标
     * @param rotation 旋转方向
     * @return 是否匹配
     */
    fun test(world: World, anchor: BlockPos, x: Int, y: Int, z: Int, rotation: MultiblockRotation): Boolean
}

/**
 * 模拟结果，表示多方块中的一个组成方块
 */
data class SimulateResult(
    /** 世界中的实际位置 */
    val worldPosition: BlockPos,
    /** 状态匹配器 */
    val stateMatcher: IStateMatcher,
    /** 在图案中对应的字符（仅 DenseMultiblock 有值） */
    val character: Char? = null
)
