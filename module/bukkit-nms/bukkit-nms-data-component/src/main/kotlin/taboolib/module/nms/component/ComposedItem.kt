package taboolib.module.nms.component

import org.bukkit.inventory.ItemStack
import org.tabooproject.reflex.Reflex.Companion.invokeConstructor
import org.tabooproject.reflex.ReflexClass
import taboolib.module.chat.ComponentText
import taboolib.module.nms.component.ComposedItem.Companion.obcClass
import taboolib.module.nms.obcClass
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * ComposedItem
 *
 * @author TheFloodDragon
 * @since 2026/2/19 17:11
 */
@Suppress("unused")
open class ComposedItem protected constructor(
    /**
     * [ItemStack] 处理对象, 必为 CraftItemStack
     */
    val handle: ItemStack
) {

    // region 组件操作

    // ── 文本组件 ──

    /** 物品自定义名称（覆盖默认名称，斜体显示）**/
    var customName: ComponentText? by composed(ItemComponents.CUSTOM_NAME)

    /** 物品基础名称（不覆盖，不显示为斜体，1.20.5+）**/
    var itemName: ComponentText? by composed(ItemComponents.ITEM_NAME)

    /** 物品描述 Lore **/
    var lore: MutableList<ComponentText> by composed(ItemComponents.LORE, mutableListOf())

    // ── 数值组件 ──

    /** 物品当前耐久损耗值 **/
    var damage: Int by composed(ItemComponents.DAMAGE, 0)

    /** 物品最大耐久值（1.20.5+）**/
    var maxDamage: Int? by composed(ItemComponents.MAX_DAMAGE)

    /** 物品最大堆叠数量（1.20.5+）**/
    var maxStackSize: Int? by composed(ItemComponents.MAX_STACK_SIZE)

    /** 铁砧修复所需经验倍率**/
    var repairCost: Int? by composed(ItemComponents.REPAIR_COST)

    /** 自定义模型数据（1.20.5+）**/
    var customModelData: Int? by composed(ItemComponents.CUSTOM_MODEL_DATA)

    // ── 布尔/标记组件 ──

    /** 无法破坏 **/
    var unbreakable: Boolean by composed(ItemComponents.UNBREAKABLE, false)

    /** 附魔光效强制覆盖 (true=强制显示,false=强制隐藏,null=移除覆盖) (仅1.20.5+)**/
    var enchantmentGlintOverride: Boolean? by composed(ItemComponents.ENCHANTMENT_GLINT_OVERRIDE)

    /** 防火属性（物品不会被熔岩/火焰销毁，1.20.5+）**/
    var fireResistant: Boolean by composed(ItemComponents.FIRE_RESISTANT, false)

    /** 完全隐藏提示框（1.20.5+）**/
    var hideTooltip: Boolean by composed(ItemComponents.HIDE_TOOLTIP, false)

    /** 隐藏额外提示信息（保留名称和 Lore，1.20.5+）**/
    var hideAdditionalTooltip: Boolean by composed(ItemComponents.HIDE_ADDITIONAL_TOOLTIP, false)

    // endregion

    /** 获取组件 **/
    operator fun <T : Any> get(type: ComposedType<T>): T? {
        return nmsHandle?.let { type.get(it) }
    }

    /** 设置组件 **/
    operator fun <T : Any> set(type: ComposedType<T>, value: T) {
        nmsHandle?.let { type.set(it, value) }
    }

    /** 删除组件 **/
    fun remove(type: ComposedType<*>) {
        nmsHandle?.let { type.remove(it) }
    }

    /** composed -= type — 等价于 remove(type) */
    operator fun minusAssign(type: ComposedType<*>) = remove(type)

    /** 是否拥有组件 **/
    fun has(type: ComposedType<*>): Boolean = this[type] != null

    /** type in composed — 等价于 has(type) */
    operator fun contains(type: ComposedType<*>): Boolean = has(type)

    /**
     * NMS形式实例 [net.minecraft.world.item.ItemStack]
     */
    private val nmsHandle: Any? get() = obcHandleField.get(handle)

    companion object {

        /**
         * 通过 [ItemStack] 创建一个 [ComposedItem]
         */
        @JvmStatic
        fun of(itemStack: ItemStack): ComposedItem {
            return if (isObcClass(itemStack::class.java)) {
                ComposedItem(itemStack)  // CraftItemStack
            } else ComposedItem(newObc(itemStack) as ItemStack) // an impl of interface BukkitItemStack, but not CraftItemStack
        }

        /**
         * 通过 [net.minecraft.world.item.ItemStack] 创建一个 [ComposedItem]
         */
        @JvmStatic
        fun ofNms(nmsItem: Any) = ComposedItem(newObc(nmsItem) as ItemStack)

        /**
         * obc.ItemStack
         *   org.bukkit.craftbukkit.$VERSION.inventory.CraftItemStack
         */
        @JvmStatic
        val obcClass by lazy { obcClass("inventory.CraftItemStack") }

        /**
         * private CraftItemStack(net.minecraft.world.item.ItemStack item)
         * private CraftItemStack(ItemStack item)
         */
        @JvmStatic
        fun newObc(item: Any): Any = obcClass.invokeConstructor(item)

        /**
         * 检查类是否为[obcClass]
         */
        @JvmStatic
        fun isObcClass(clazz: Class<*>) = obcClass.isAssignableFrom(clazz)

        /** net.minecraft.world.item.ItemStack handle; **/
        @JvmStatic
        val obcHandleField by lazy {
            ReflexClass.of(obcClass).structure.getField("handle")
        }

    }

    /**
     * 非空属性委托（有默认值）
     */
    class ComponentDelegate<T : Any>(
        val type: ComposedType<T>,
        val default: T
    ) : ReadWriteProperty<ComposedItem, T> {
        override fun getValue(thisRef: ComposedItem, property: KProperty<*>): T = thisRef[type] ?: default
        override fun setValue(thisRef: ComposedItem, property: KProperty<*>, value: T) = thisRef.set(type, value)
    }

    /**
     * 可空属性委托（无默认值）
     */
    class NullableComponentDelegate<T : Any>(
        val type: ComposedType<T>
    ) : ReadWriteProperty<ComposedItem, T?> {
        override fun getValue(thisRef: ComposedItem, property: KProperty<*>): T? = thisRef[type]
        override fun setValue(thisRef: ComposedItem, property: KProperty<*>, value: T?) =
            if (value != null) thisRef.set(type, value) else thisRef.remove(type)
    }

    // DSL 函数
    fun <T : Any> composed(type: ComposedType<T>) = NullableComponentDelegate(type)
    fun <T : Any> composed(type: ComposedType<T>, default: T) = ComponentDelegate(type, default)

}