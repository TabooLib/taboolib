package taboolib.module.ui.type.impl

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.library.xseries.XMaterial
import taboolib.module.ui.ClickEvent
import taboolib.module.ui.ClickType
import taboolib.module.ui.MenuHolder
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Chest
import taboolib.module.ui.virtual.virtualize
import taboolib.platform.util.ItemBuilder
import taboolib.platform.util.buildItem
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

open class ChestImpl(override var title: String) : Chest {

    /** 最后一次构建的页面 */
    lateinit var lastInventory: Inventory

    /** 行数 **/
    override var rows = 1

    /** 虚拟化 */
    override var virtualized = false

    /** 虚拟化时玩家背包内容 */
    override var virtualizedStorageContents: List<ItemStack>? = null

    /** 物品与对应抽象字符关系 **/
    override var items = ConcurrentHashMap<Char, ItemStack>()

    /** 抽象字符布局 **/
    override var slots = CopyOnWriteArrayList<List<Char>>()

    /** 锁定主手 **/
    override var handLocked = true

    /** 是否打开过 **/
    override var isOpened = false

    /** MenuHolder 回调 **/
    var holderCallback: ((menu: ChestImpl) -> MenuHolder) = { MenuHolder(it) }

    /** 点击回调 **/
    val clickCallback = CopyOnWriteArrayList<(event: ClickEvent) -> Unit>()

    /** 点击回调 **/
    var selfClickCallback: (event: ClickEvent) -> Unit = {}

    /** 关闭回调 **/
    var closeCallback: ((event: InventoryCloseEvent) -> Unit) = { isOpened = false }

    /** 只触发一次关闭回调 **/
    var onceCloseCallback = false

    /** 忽略 updateTitle 的关闭回调 **/
    var isSkipCloseCallbackOnUpdateTitle = true

    /** 是否刷新标题 */
    var isUpdateTitle = false

    /** 构建回调 **/
    var buildCallback: ((player: Player, inventory: Inventory) -> Unit) = { _, _ -> isOpened = true }

    /** 异步构建回调 **/
    var asyncBuildCallback: ((player: Player, inventory: Inventory) -> Unit) = { _, _ -> }

    /** 构建回调 **/
    var selfBuildCallback: ((player: Player, inventory: Inventory) -> Unit) = { _, _ -> }

    /** 异步构建回调 **/
    var selfAsyncBuildCallback: ((player: Player, inventory: Inventory) -> Unit) = { _, _ -> }

    /**
     * 使用虚拟页面（将自动阻止所有点击行为）
     */
    override fun virtualize(storageContents: List<ItemStack>? ) {
        this.virtualized = true
        this.virtualizedStorageContents = storageContents
    }

    /**
     * 隐藏玩家背包（自动启动虚拟页面）
     */
    override fun hidePlayerInventory() {
        virtualize((0 until 36).map { ItemStack(Material.AIR) })
    }

    /**
     * 行数
     * 为 1 - 6 之间的整数，并非原版 9 的倍数
     */
    override fun rows(rows: Int) {
        this.rows = rows
    }

    /**
     * 设置是否锁定玩家手部动作
     * 设置为 true 则将阻止玩家在使用菜单时进行包括但不限于
     * 丢弃物品，拿出菜单物品等行为
     *
     * @param handLocked 锁定
     */
    override fun handLocked(handLocked: Boolean) {
        this.handLocked = handLocked
    }

    /**
     * 设置 MenuHolder 创建回调
     */
    override fun holder(func: (menu: Chest) -> MenuHolder) {
        this.holderCallback = func
    }

    /**
     * 页面构建时触发回调
     * 可选是否异步执行
     */
    override fun onBuild(async: Boolean, callback: (player: Player, inventory: Inventory) -> Unit) {
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

    open fun selfBuild(async: Boolean = false, callback: (player: Player, inventory: Inventory) -> Unit) {
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
    override fun onClose(once: Boolean, skipUpdateTitle: Boolean, callback: (event: InventoryCloseEvent) -> Unit) {
        closeCallback = callback
        onceCloseCallback = once
        isSkipCloseCallbackOnUpdateTitle = skipUpdateTitle
    }

    /**
     * 点击事件回调
     * 仅在特定位置下触发
     */
    override fun onClick(bind: Int, callback: (event: ClickEvent) -> Unit) {
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
    override fun onClick(bind: Char, callback: (event: ClickEvent) -> Unit) {
        onClick {
            if (it.slot == bind) {
                it.isCancelled = true
                // 只处理 CLICK 类型
                if (it.clickType != ClickType.DRAG) {
                    callback(it)
                }
            }
        }
    }

    /**
     * 整页点击事件回调
     * 可选是否自动锁定点击位置
     */
    override fun onClick(lock: Boolean, callback: (event: ClickEvent) -> Unit) {
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

    open fun selfClick(callback: (event: ClickEvent) -> Unit) {
        selfClickCallback = callback
    }

    /**
     * 使用抽象字符页面布局
     */
    override fun map(vararg slots: String) {
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
    override fun set(slot: Char, itemStack: ItemStack) {
        if (isOpened) error("Menu has been opened, cannot preset item.")
        items[slot] = itemStack
    }

    /**
     * 根据位置设置物品
     */
    override fun set(slot: Int, itemStack: ItemStack) {
        onBuild { _, it -> it.setItem(slot, itemStack) }
    }

    /**
     * 根据抽象符号设置物品
     */
    override fun set(slot: Char, callback: () -> ItemStack) {
        onBuild { _, it -> getSlots(slot).forEach { s -> it.setItem(s, callback()) } }
    }

    /**
     * 根据位置设置物品
     */
    override fun set(slot: Int, callback: () -> ItemStack) {
        onBuild { _, it -> it.setItem(slot, callback()) }
    }

    /**
     * 根据抽象符号设置物品
     */
    override fun set(slot: Char, material: XMaterial, itemBuilder: ItemBuilder.() -> Unit) {
        set(slot, buildItem(material, itemBuilder))
    }

    /**
     * 根据位置设置物品
     */
    override fun set(slot: Int, material: XMaterial, itemBuilder: ItemBuilder.() -> Unit) {
        set(slot, buildItem(material, itemBuilder))
    }

    /**
     * 根据抽象符号设置物品
     */
    override fun set(slot: Char, itemStack: ItemStack, onClick: ClickEvent.() -> Unit) {
        set(slot, itemStack)
        onClick(slot, onClick)
    }

    /**
     * 根据位置设置物品
     */
    override fun set(slot: Int, itemStack: ItemStack, onClick: ClickEvent.() -> Unit) {
        set(slot, itemStack)
        onClick(slot, onClick)
    }

    /**
     * 获取位置对应的抽象字符
     */
    override fun getSlot(slot: Int): Char {
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
    override fun getSlots(slot: Char): List<Int> {
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
    override fun getFirstSlot(slot: Char): Int {
        val slots = getSlots(slot)
        return if (slots.isEmpty()) -1 else slots[0]
    }

    /**
     * 更新标题
     */
    override fun updateTitle(title: String) {
        this.title = title
        this.isUpdateTitle = true
        try {
            // 获取所有打开页面的玩家
            val viewers = lastInventory.viewers.toList()
            // 重新构建页面
            build()
            // 重新打开页面
            viewers.forEach { it.openMenu(lastInventory, changeId = false) }
        } catch (ex: Throwable) {
            ex.printStackTrace()
        }
        this.isUpdateTitle = false
    }

    /**
     * 创建标题
     */
    open fun createTitle(): String {
        return title
    }

    /**
     * 构建页面
     */
    override fun build(): Inventory {
        lastInventory = Bukkit.createInventory(holderCallback(this), if (rows > 0) rows * 9 else slots.size * 9, createTitle())
        // 虚拟化
        if (virtualized) {
            lastInventory = lastInventory.virtualize(virtualizedStorageContents)
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