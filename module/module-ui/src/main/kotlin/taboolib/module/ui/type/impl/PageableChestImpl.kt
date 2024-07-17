package taboolib.module.ui.type.impl

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.common.util.subList
import taboolib.module.ui.ClickEvent
import taboolib.module.ui.type.PageableChest
import taboolib.module.ui.virtual.VirtualInventory
import taboolib.module.ui.virtual.inject
import taboolib.module.ui.virtual.openVirtualInventory
import taboolib.platform.util.isNotAir
import java.util.concurrent.CopyOnWriteArrayList

open class PageableChestImpl<T>(title: String) : ChestImpl(title), PageableChest<T> {

    /** 页数 **/
    override var page = 0

    /** 页面玩家 **/
    lateinit var viewer: Player

    /** 锁定所有位置 **/
    var menuLocked = true

    /** 页面可用位置 **/
    val menuSlots = CopyOnWriteArrayList<Int>()

    /** 页面可用元素回调 **/
    var elementsCallback: (() -> List<T>) = { CopyOnWriteArrayList() }

    /** 页面可用元素缓存 **/
    var elementsCache = emptyList<T>()

    /** 点击事件回调 **/
    var elementClickCallback: ((event: ClickEvent, element: T) -> Unit) = { _, _ -> }

    /** 元素生成回调 **/
    var generateCallback: ((player: Player, element: T, index: Int, slot: Int) -> ItemStack) = { _, _, _, _ -> ItemStack(Material.AIR) }

    /** 异步元素生成回调 **/
    var asyncGenerateCallback: ((player: Player, element: T, index: Int, slot: Int) -> ItemStack) = { _, _, _, _ -> ItemStack(Material.AIR) }

    /** 页面切换回调 */
    var pageChangeCallback: ((player: Player) -> Unit) = { _ -> }

    /**
     * 是否锁定所有位置
     * 默认为 true
     */
    override fun menuLocked(lockAll: Boolean) {
        this.menuLocked = lockAll
    }

    /**
     * 设置页数
     */
    override fun page(page: Int) {
        this.page = page
    }

    /**
     * 设置可用位置
     */
    override fun slots(slots: List<Int>) {
        this.menuSlots.clear()
        this.menuSlots += slots
    }

    /**
     * 通过抽象字符选择由 map 函数铺设的页面位置
     */
    override fun slotsBy(char: Char) {
        slots(getSlots(char))
    }

    /**
     * 可用元素列表回调
     */
    override fun elements(elements: () -> List<T>) {
        elementsCallback = elements
    }

    /**
     * 元素对应物品生成回调
     */
    override fun onGenerate(async: Boolean, callback: (player: Player, element: T, index: Int, slot: Int) -> ItemStack) {
        if (async) {
            asyncGenerateCallback = callback
        } else {
            generateCallback = callback
        }
    }

    /**
     * 页面构建回调
     */
    override fun onBuild(async: Boolean, callback: (inventory: Inventory) -> Unit) {
        onBuild(async = async) { _, inventory -> callback(inventory) }
    }

    /**
     * 元素点击回调
     */
    override fun onClick(callback: (event: ClickEvent, element: T) -> Unit) {
        elementClickCallback = callback
    }

    /**
     * 设置下一页按钮
     */
    override fun setNextPage(slot: Int, callback: (page: Int, hasNextPage: Boolean) -> ItemStack) {
        // 设置物品
        set(slot) { callback(page, hasNextPage()) }
        // 点击事件
        onClick(slot) {
            if (hasNextPage()) {
                page++
                // 刷新页面
                if (virtualized) {
                    viewer.openVirtualInventory(build() as VirtualInventory).inject(this)
                } else {
                    viewer.openInventory(build())
                }
                pageChangeCallback(viewer)
            }
        }
    }

    /**
     * 设置上一页按钮
     */
    override fun setPreviousPage(slot: Int, callback: (page: Int, hasPreviousPage: Boolean) -> ItemStack) {
        // 设置物品
        set(slot) { callback(page, hasPreviousPage()) }
        // 点击事件
        onClick(slot) {
            if (hasPreviousPage()) {
                page--
                // 刷新页面
                if (virtualized) {
                    viewer.openVirtualInventory(build() as VirtualInventory).inject(this)
                } else {
                    viewer.openInventory(build())
                }
                pageChangeCallback(viewer)
            }
        }
    }

    /**
     * 切换页面回调
     */
    override fun onPageChange(callback: (player: Player) -> Unit) {
        pageChangeCallback = callback
    }

    /**
     * 是否可以返回上一页
     */
    override fun hasPreviousPage(): Boolean {
        return page > 0
    }

    /**
     * 是否可以前往下一页
     */
    override fun hasNextPage(): Boolean {
        return isNext(page, elementsCache.size, menuSlots.size)
    }

    override fun createTitle(): String {
        return title.replace("%p", (page + 1).toString())
    }

    override fun resetElementsCache() {
        elementsCache = elementsCallback()
    }

    /**
     * 构建页面
     */
    override fun build(): Inventory {
        // 更新元素列表缓存
        elementsCache = elementsCallback()

        // 本次页面所使用的元素缓存
        val elementMap = hashMapOf<Int, T>()
        val elementItems = subList(elementsCache, page * menuSlots.size, (page + 1) * menuSlots.size)

        /**
         * 构建事件处理函数
         */
        fun processBuild(p: Player, inventory: Inventory, async: Boolean) {
            viewer = p
            elementItems.forEachIndexed { index, item ->
                val slot = menuSlots.getOrNull(index) ?: 0
                elementMap[slot] = item
                // 生成元素对应物品
                val callback = if (async) asyncGenerateCallback else generateCallback
                val itemStack = callback(viewer, item, index, slot)
                if (itemStack.isNotAir()) {
                    inventory.setItem(slot, itemStack)
                }
            }
        }

        // 生成回调
        selfBuild { p, it -> processBuild(p, it, false) }
        // 生成异步回调
        selfBuild(async = true) { p, it -> processBuild(p, it, true) }
        // 生成点击回调
        selfClick {
            if (menuLocked) {
                it.isCancelled = true
            }
            elementClickCallback(it, elementMap[it.rawSlot] ?: return@selfClick)
        }
        // 构建页面
        return super.build()
    }

    /**
     * 是否存在下一页
     */
    private fun isNext(page: Int, size: Int, entry: Int): Boolean {
        return size / entry.toDouble() > page + 1
    }
}