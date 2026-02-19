package taboolib.module.nms.component

import org.bukkit.inventory.ItemStack


/**
 * 获取 [ComposedItem] 工具
 */
fun ItemStack.composed(copy: Boolean = true): ComposedItem {
    val itemStack = if (copy) this.clone() else this
    return ComposedItem.of(itemStack)
}

/**
 * 获取 [ComposedItem] 工具
 */
fun ItemStack.compose(copy: Boolean = true, block: ComposedItem.() -> ComposedItem): ComposedItem {
    return composed(copy).run(block)
}
