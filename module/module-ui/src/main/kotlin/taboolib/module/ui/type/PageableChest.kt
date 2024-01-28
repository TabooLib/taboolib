package taboolib.module.ui.type

import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.module.ui.ClickEvent

/**
 * 可翻页容器
 */
interface PageableChest<T> : Chest {

    /** 页数 **/
    val page: Int

    /**
     * 是否锁定所有位置
     * 默认为 true
     */
    fun menuLocked(lockAll: Boolean)

    /**
     * 设置页数
     */
    fun page(page: Int)

    /**
     * 设置可用位置
     */
    fun slots(slots: List<Int>)

    /**
     * 通过抽象字符选择由 map 函数铺设的页面位置
     */
    fun slotsBy(char: Char)

    /**
     * 可用元素列表回调
     */
    fun elements(elements: () -> List<T>)

    /**
     * 元素对应物品生成回调
     */
    fun onGenerate(async: Boolean = false, callback: (player: Player, element: T, index: Int, slot: Int) -> ItemStack)

    /**
     * 页面构建回调
     */
    fun onBuild(async: Boolean, callback: (inventory: Inventory) -> Unit)

    /**
     * 元素点击回调
     */
    fun onClick(callback: (event: ClickEvent, element: T) -> Unit)

    /**
     * 设置下一页按钮
     */
    fun setNextPage(slot: Int, callback: (page: Int, hasNextPage: Boolean) -> ItemStack)

    /**
     * 设置上一页按钮
     */
    fun setPreviousPage(slot: Int, callback: (page: Int, hasPreviousPage: Boolean) -> ItemStack)

    /**
     * 切换页面回调
     */
    fun onPageChange(callback: (player: Player) -> Unit)

    /**
     * 是否可以返回上一页
     */
    fun hasPreviousPage(): Boolean

    /**
     * 是否可以前往下一页
     */
    fun hasNextPage(): Boolean

    /**
     * 重制元素列表缓存
     */
    fun resetElementsCache()
}