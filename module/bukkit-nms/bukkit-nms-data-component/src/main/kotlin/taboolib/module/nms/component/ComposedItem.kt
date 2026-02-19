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
open class ComposedItem protected constructor(
    /**
     * [ItemStack] 处理对象, 必为 CraftItemStack
     */
    val handle: ItemStack
) {

    // region 组件操作

    /** 物品自定义名称 **/
    var customName: ComponentText? by composed(ItemComponents.CUSTOM_NAME)

    /** 物品描述 Lore **/
    var lore: List<ComponentText>? by composed(ItemComponents.LORE)

    /** 无法破坏 **/
    var unbreakable: Boolean? by composed(ItemComponents.UNBREAKABLE)

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
     * 可空属性委托
     */
    private class ComponentDelegate<T : Any>(
        val type: ComposedType<T>
    ) : ReadWriteProperty<ComposedItem, T?> {

        override fun getValue(thisRef: ComposedItem, property: KProperty<*>): T? {
            return thisRef[type]
        }

        override fun setValue(thisRef: ComposedItem, property: KProperty<*>, value: T?) {
            if (value == null) {
                thisRef.remove(type)
            } else {
                thisRef[type] = value
            }
        }

    }

    // DSL 函数
    private fun <T : Any> composed(type: ComposedType<T>) = ComponentDelegate(type)

}