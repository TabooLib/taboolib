package taboolib.module.ui.type

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.library.xseries.XMaterial
import taboolib.module.ui.*
import taboolib.module.ui.virtual.virtualize
import taboolib.platform.util.ItemBuilder
import taboolib.platform.util.buildItem
import taboolib.platform.util.giveItem
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

open class Basic(title: String = "chest") : Menu(title) {

    /** 虚拟化 */
    internal var virtual = false

    /** 最后一次构建的页面 */
    internal lateinit var lastInventory: Inventory

    /** 虚拟化时玩家背包内容 */
    internal var storageContents: List<ItemStack>? = null

    /** 行数 **/
    internal var rows = -1

    /** 锁定主手 **/
    internal var handLocked = true

    /** MenuHolder 回调 **/
    internal var holderCallback: ((menu: Basic) -> MenuHolder) = { MenuHolder(it) }

    /** 点击回调 **/
    internal val clickCallback = CopyOnWriteArrayList<(event: ClickEvent) -> Unit>()

    /** 点击回调 **/
    internal var selfClickCallback: (event: ClickEvent) -> Unit = {}

    /** 关闭回调 **/
    internal var closeCallback: ((event: InventoryCloseEvent) -> Unit) = { isOpened = false }

    /** 只触发一次关闭回调 **/
    internal var onceCloseCallback = false

    /** 忽略 updateTitle 的关闭回调 **/
    internal var skipCloseCallbackOnUpdateTitle = true

    /** 是否刷新标题 */
    internal var isUpdateTitle = false

    /** 构建回调 **/
    internal var buildCallback: ((player: Player, inventory: Inventory) -> Unit) = { _, _ -> isOpened = true }

    /** 异步构建回调 **/
    internal var asyncBuildCallback: ((player: Player, inventory: Inventory) -> Unit) = { _, _ -> }

    /** 构建回调 **/
    internal var selfBuildCallback: ((player: Player, inventory: Inventory) -> Unit) = { _, _ -> }

    /** 异步构建回调 **/
    internal var selfAsyncBuildCallback: ((player: Player, inventory: Inventory) -> Unit) = { _, _ -> }

    /** 物品与对应抽象字符关系 **/
    var items = ConcurrentHashMap<Char, ItemStack>()

    /** 抽象字符布局 **/
    var slots = CopyOnWriteArrayList<List<Char>>()

    /** 是否打开过 **/
    var isOpened = false

    /**
     * 使用虚拟页面（将自动阻止所有点击行为）
     */
    open fun virtualize(storageContents: List<ItemStack>? = null) {
        this.virtual = true
        this.storageContents = storageContents
    }

    /**
     * 隐藏玩家背包（自动启动虚拟页面）
     */
    open fun hidePlayerInventory() {
        virtualize((0 until 36).map { ItemStack(Material.AIR) })
    }

    /**
     * 行数
     * 为 1 - 6 之间的整数，并非原版 9 的倍数
     */
    open fun rows(rows: Int) {
        this.rows = rows
    }

    /**
     * 设置是否锁定玩家手部动作
     * 设置为 true 则将阻止玩家在使用菜单时进行包括但不限于
     * 丢弃物品，拿出菜单物品等行为
     *
     * @param handLocked 锁定
     */
    open fun handLocked(handLocked: Boolean) {
        this.handLocked = handLocked
    }

    open fun holder(func: (menu: Basic) -> MenuHolder) {
        this.holderCallback = func
    }

    /**
     * 页面构建时触发回调
     * 可选是否异步执行
     */
    open fun onBuild(async: Boolean = false, callback: (player: Player, inventory: Inventory) -> Unit) {
        if (isOpened) error("Menu has been opened, cannot set build callback.")
        if (async) {
            val before = asyncBuildCallback
            asyncBuildCallback = { player, inventory ->
                callback(player, inventory)
                before(player, inventory)
            }
        } else {
            val before = buildCallback
            buildCallback = { player, inventory ->
                callback(player, inventory)
                before(player, inventory)
            }
        }
    }

    protected open fun selfBuild(async: Boolean = false, callback: (player: Player, inventory: Inventory) -> Unit) {
        if (async) {
            selfAsyncBuildCallback = callback
        } else {
            selfBuildCallback = callback
        }
    }

    /**
     * 页面关闭时触发回调
     * 只能触发一次（玩家客户端强制关闭时会触发两次原版 InventoryCloseEvent 事件）
     *
     * TODO 2023/10/09 若启用虚拟化菜单，则 player.closeInventory() 不会触发该回调函数
     */
    open fun onClose(once: Boolean = true, skipUpdateTitle: Boolean = true, callback: (event: InventoryCloseEvent) -> Unit) {
        closeCallback = callback
        onceCloseCallback = once
        skipCloseCallbackOnUpdateTitle = skipUpdateTitle
    }

    /**
     * 点击事件回调
     * 仅在特定位置下触发
     */
    open fun onClick(bind: Int, callback: (event: ClickEvent) -> Unit = {}) {
        onClick {
            if (it.rawSlot == bind) {
                it.isCancelled = true
                // 只处理 CLICK 类型
                if (it.clickType != ClickType.DRAG) {
                    callback(it)
                }
            }
        }
    }

    /**
     * 点击事件回调
     * 仅在特定位置下触发
     */
    open fun onClick(bind: Char, callback: (event: ClickEvent) -> Unit = {}) {
        onClick {
            if (it.slot == bind) {
                it.isCancelled = true
                // 只处理 CLICK 类型
                if (it.clickType != ClickType.DRAG ) {
                    callback(it)
                }
            }
        }
    }

    /**
     * 整页点击事件回调
     * 可选是否自动锁定点击位置
     */
    open fun onClick(lock: Boolean = false, callback: (event: ClickEvent) -> Unit = {}) {
        if (lock) {
            clickCallback += {
                it.isCancelled = true
                // 只处理 CLICK 类型
                if (it.clickType != ClickType.DRAG) {
                    callback(it)
                }
            }
        } else {
            clickCallback += callback
        }
    }

    protected open fun selfClick(callback: (event: ClickEvent) -> Unit = {}) {
        selfClickCallback = callback
    }

    /**
     * 使用抽象字符页面布局
     */
    open fun map(vararg slots: String) {
        this.slots.clear()
        this.slots.addAll(slots.map { it.toCharArray().toList() })
        // 自动修改行数
        if (rows < slots.size) {
            rows = slots.size
        }
    }

    /**
     * 根据抽象符号设置物品
     */
    open fun set(slot: Char, itemStack: ItemStack) {
        if (isOpened) error("Menu has been opened, cannot preset item.")
        items[slot] = itemStack
    }

    /**
     * 根据位置设置物品
     */
    open fun set(slot: Int, itemStack: ItemStack) {
        onBuild { _, it -> it.setItem(slot, itemStack) }
    }

    /**
     * 根据抽象符号设置物品
     */
    open fun set(slot: Char, callback: () -> ItemStack) {
        onBuild { _, it -> getSlots(slot).forEach { s -> it.setItem(s, callback()) } }
    }

    /**
     * 根据位置设置物品
     */
    open fun set(slot: Int, callback: () -> ItemStack) {
        onBuild { _, it -> it.setItem(slot, callback()) }
    }

    /**
     * 根据抽象符号设置物品
     */
    open fun set(slot: Char, material: XMaterial, itemBuilder: ItemBuilder.() -> Unit = {}) {
        set(slot, buildItem(material, itemBuilder))
    }

    /**
     * 根据位置设置物品
     */
    open fun set(slot: Int, material: XMaterial, itemBuilder: ItemBuilder.() -> Unit = {}) {
        set(slot, buildItem(material, itemBuilder))
    }

    /**
     * 根据抽象符号设置物品
     */
    open fun set(slot: Char, itemStack: ItemStack, onClick: ClickEvent.() -> Unit = {}) {
        set(slot, itemStack)
        onClick(slot, onClick)
    }

    /**
     * 根据位置设置物品
     */
    open fun set(slot: Int, itemStack: ItemStack, onClick: ClickEvent.() -> Unit = {}) {
        set(slot, itemStack)
        onClick(slot, onClick)
    }

    /**
     * 获取位置对应的抽象字符
     */
    open fun getSlot(slot: Int): Char {
        var row = 0
        while (row < slots.size) {
            val line = slots[row]
            var cel = 0
            while (cel < line.size && cel < 9) {
                if (row * 9 + cel == slot) {
                    return line[cel]
                }
                cel++
            }
            row++
        }
        return ' '
    }

    /**
     * 获取抽象字符对应的位置
     */
    open fun getSlots(slot: Char): List<Int> {
        val list = mutableListOf<Int>()
        var row = 0
        while (row < slots.size) {
            val line = slots[row]
            var cel = 0
            while (cel < line.size && cel < 9) {
                if (line[cel] == slot) {
                    list.add(row * 9 + cel)
                }
                cel++
            }
            row++
        }
        return list
    }

    /**
     * 获取抽象字符对应的首个位置
     */
    open fun getFirstSlot(slot: Char): Int {
        val slots = getSlots(slot)
        return if (slots.isEmpty()) -1 else slots[0]
    }

    /**
     * 在页面关闭时返还物品
     *
     * @param slots 对应格子
     */
    fun InventoryCloseEvent.returnItems(slots: List<Int>) {
        slots.forEach { player.giveItem(inventory.getItem(it)) }
    }

    /**
     * 更新标题
     */
    fun updateTitle(title: String) {
        this.title = title
        this.isUpdateTitle = true
        try {
            // 获取所有打开页面的玩家
            val viewers = lastInventory.viewers.toList()
            // 重新构建页面
            build()
            // 重新打开页面
            viewers.forEach { it.openMenu(lastInventory) }
        } catch (ex: Throwable) {
            ex.printStackTrace()
        }
        this.isUpdateTitle = false
    }

    protected open fun createTitle(): String {
        return title
    }

    /**
     * 构建页面
     */
    override fun build(): Inventory {
        lastInventory = Bukkit.createInventory(holderCallback(this), if (rows > 0) rows * 9 else slots.size * 9, createTitle())
        // 虚拟化
        if (virtual) {
            lastInventory = lastInventory.virtualize(storageContents)
        }
        var row = 0
        while (row < slots.size) {
            val line = slots[row]
            var cel = 0
            while (cel < line.size && cel < 9) {
                lastInventory.setItem(row * 9 + cel, items[line[cel]] ?: ItemStack(Material.AIR))
                cel++
            }
            row++
        }
        return lastInventory
    }
}