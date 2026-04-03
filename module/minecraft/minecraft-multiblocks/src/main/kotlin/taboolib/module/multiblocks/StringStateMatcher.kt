package taboolib.module.multiblocks

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.Block

/**
 * 字符串解析的状态匹配器
 *
 * 支持以下格式：
 * - `minecraft:stone` — 匹配指定材质（忽略状态属性）
 * - `minecraft:oak_stairs[facing=north,half=bottom]` — 精确匹配方块数据
 * - `#minecraft:wool` — 标签匹配（以 # 开头，匹配标签中的所有方块）
 *
 * @author FxRayHughes
 * @since 2026/3/30
 */
object StringStateMatcher {

    /**
     * 从字符串解析状态匹配器
     *
     * @param input 匹配器字符串
     * @return 解析后的状态匹配器
     * @throws IllegalArgumentException 无法解析时
     */
    fun parse(input: String): IStateMatcher {
        val str = input.trim()
        // 标签匹配
        if (str.startsWith("#")) {
            return TagMatcher(str.substring(1))
        }
        // 包含方块属性的精确匹配
        if (str.contains("[")) {
            return BlockDataMatcher(str)
        }
        // 简单材质匹配
        return MaterialMatcher(str)
    }

    /**
     * 材质匹配器
     */
    private class MaterialMatcher(val materialName: String) : IStateMatcher {

        private val material: Material? by lazy {
            Material.matchMaterial(materialName)
        }

        override val displayName: String get() = materialName

        override fun test(block: Block): Boolean {
            return material != null && block.type == material
        }
    }

    /**
     * 精确方块数据匹配器
     */
    private class BlockDataMatcher(val blockDataString: String) : IStateMatcher {

        override val displayName: String get() = blockDataString

        override fun test(block: Block): Boolean {
            return try {
                val expected = Bukkit.createBlockData(blockDataString)
                block.blockData.matches(expected)
            } catch (_: Exception) {
                false
            }
        }
    }

    /**
     * 标签匹配器（匹配方块标签中的所有方块）
     */
    private class TagMatcher(val tagName: String) : IStateMatcher {

        override val displayName: String get() = "#$tagName"

        override fun test(block: Block): Boolean {
            return Bukkit.getTags(Tag.REGISTRY_BLOCKS, Material::class.java).any { tag ->
                tag.key.toString() == tagName && tag.isTagged(block.type)
            }
        }
    }
}
