package taboolib.module.multiblocks

import org.bukkit.block.Block

/**
 * 方块状态匹配器，用于判断世界中某个位置的方块是否符合预期
 *
 * 参考 Patchouli 的 IStateMatcher 设计，适配 Bukkit 平台。
 *
 * @author FxRayHughes
 * @since 2026/3/30
 */
interface IStateMatcher {

    /**
     * 用于显示的方块名称
     */
    val displayName: String

    /**
     * 测试给定的方块是否匹配
     *
     * @param block 世界中的方块
     * @return 是否匹配
     */
    fun test(block: Block): Boolean
}
