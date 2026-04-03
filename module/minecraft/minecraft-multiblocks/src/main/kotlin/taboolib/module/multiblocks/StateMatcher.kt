package taboolib.module.multiblocks

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData

/**
 * 内置的方块状态匹配器实现
 *
 * 提供常用的匹配器工厂方法和内置常量。
 *
 * @author FxRayHughes
 * @since 2026/3/30
 */
object StateMatcher {

    /** 匹配任意方块（包括空气） */
    val ANY: IStateMatcher = object : IStateMatcher {
        override val displayName = "any"
        override fun test(block: Block) = true
    }

    /** 仅匹配空气方块 */
    val AIR: IStateMatcher = object : IStateMatcher {
        override val displayName = "air"
        override fun test(block: Block) = block.type.isAir
    }

    /**
     * 匹配指定材质（忽略方块状态属性）
     *
     * @param material 目标材质
     */
    fun fromMaterial(material: Material): IStateMatcher {
        return object : IStateMatcher {
            override val displayName = material.key.toString()
            override fun test(block: Block) = block.type == material
        }
    }

    /**
     * 精确匹配方块数据（包括状态属性）
     *
     * @param blockData 目标方块数据
     */
    fun fromBlockData(blockData: BlockData): IStateMatcher {
        return object : IStateMatcher {
            override val displayName = blockData.asString
            override fun test(block: Block) = block.blockData.matches(blockData)
        }
    }

    /**
     * 宽松匹配方块数据（仅检查指定的状态属性，忽略未指定的属性）
     *
     * @param blockData 目标方块数据
     */
    fun fromBlockDataLoose(blockData: BlockData): IStateMatcher {
        return object : IStateMatcher {
            override val displayName = blockData.asString
            override fun test(block: Block) = block.blockData.matches(blockData)
        }
    }

    /**
     * 自定义谓词匹配
     *
     * @param name 显示名称
     * @param predicate 匹配谓词
     */
    fun fromPredicate(name: String, predicate: (Block) -> Boolean): IStateMatcher {
        return object : IStateMatcher {
            override val displayName = name
            override fun test(block: Block) = predicate(block)
        }
    }

    /**
     * 仅用于显示，不进行实际验证（始终返回 true）
     *
     * @param name 显示名称
     */
    fun displayOnly(name: String): IStateMatcher {
        return object : IStateMatcher {
            override val displayName = name
            override fun test(block: Block) = true
        }
    }
}
