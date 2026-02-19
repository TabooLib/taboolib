package taboolib.module.nms.component

import taboolib.common.io.taboolibPath
import taboolib.module.chat.ComponentText

/**
 * ItemComponents
 * 
 * @author TheFloodDragon
 * @since 2026/2/19 16:16
 */
object ItemComponents {

    /**
     * 已实现的物品组件注册表
     */
    val registry: MutableCollection<ComposedType<*>> = mutableListOf()

    /** 物品自定义名称 */
    val CUSTOM_NAME = r<ComponentText>("CustomNameType")

    /** 物品描述 Lore */
    val LORE = r<List<ComponentText>>("LoreType")

    // TODO 其他组件和低版本支持

    /**
     * 私有注册函数
     */
    private fun <T : Any> r(name: String): ComposedType<T> {
        val fullName = "$taboolibPath.module.nms.component.types.$name"
        val type = ComposedType.of<T>(fullName)
        registry.add(type)
        return type
    }

}