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

    // ========== 文本组件 ==========

    /** 物品自定义名称（覆盖默认名称，斜体显示）**/
    val CUSTOM_NAME = r<ComponentText>("CustomNameType")

    /** 物品基础名称（不覆盖，不显示为斜体）**/
    val ITEM_NAME = r<ComponentText>("ItemNameType")

    /** 物品描述 Lore **/
    val LORE = r<MutableList<ComponentText>>("LoreType")

    // ========== 数值组件 ==========

    /** 物品当前耐久损耗值 **/
    val DAMAGE = r<Int>("DamageType")

    /** 物品最大耐久值 **/
    val MAX_DAMAGE = r<Int>("MaxDamageType")

    /** 物品最大堆叠数量 **/
    val MAX_STACK_SIZE = r<Int>("MaxStackSizeType")

    /** 铁砧修复所需经验倍率 **/
    val REPAIR_COST = r<Int>("RepairCostType")

    /** 自定义模型数据（用于资源包模型切换）**/
    val CUSTOM_MODEL_DATA = r<Int>("CustomModelDataType")

    // ========== 布尔/标记组件 ==========

    /** 无法破坏 **/
    val UNBREAKABLE = r<Boolean>("UnbreakableType")

    /** 附魔光效强制覆盖（true=强制显示，false=强制隐藏，null=默认行为）**/
    val ENCHANTMENT_GLINT_OVERRIDE = r<Boolean>("EnchantmentGlintOverrideType")

    /** 防火属性（物品不会被熔岩/火焰销毁）**/
    val FIRE_RESISTANT = r<Boolean>("FireResistantType")

    /** 完全隐藏提示框 **/
    val HIDE_TOOLTIP = r<Boolean>("HideTooltipType")

    /** 隐藏额外提示信息（保留名称和 Lore）**/
    val HIDE_ADDITIONAL_TOOLTIP = r<Boolean>("HideAdditionalTooltipType")

    // TODO 附魔、食物、工具、盔甲纹饰等复合组件

    /**
     * 私有注册函数
     */
    private fun <T : Any> r(name: String): ComposedType<T> {
        val fullName = "$taboolibPath.module.nms.component.internal.$name"
        val type = ComposedType.of<T>(fullName)
        registry.add(type)
        return type
    }

}