package taboolib.module.ui.type

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.library.xseries.XMaterial
import taboolib.module.ui.ClickEvent
import taboolib.module.ui.ClickType
import taboolib.module.ui.Menu
import taboolib.module.ui.MenuHolder
import taboolib.platform.util.ItemBuilder
import taboolib.platform.util.buildItem
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

open class Basic(title: String = "chest") : Menu(title) {

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
    internal var closeCallback: ((event: InventoryCloseEvent) -> Unit) = {}

    /** 构建回调 **/
    internal var buildCallback: ((player: Player, inventory: Inventory) -> Unit) = { _, _ -> }

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
            selfBuildCallback = callback
        } else {
            selfAsyncBuildCallback = callback
        }
    }

    /**
     * 页面关闭时触发回调
     * 只能触发一次（玩家客户端强制关闭时会触发两次原版 InventoryCloseEvent 事件）
     */
    open fun onClose(callback: (event: InventoryCloseEvent) -> Unit) {
        closeCallback = callback
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
                if (it.clickType == ClickType.CLICK) {
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
                if (it.clickType == ClickType.CLICK) {
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
                if (it.clickType == ClickType.CLICK) {
                    callback(it)
                }
            }
        } else {
            clickCallback += callback
        }
    }

    protected open fun selfClick(lock: Boolean = false, callback: (event: ClickEvent) -> Unit = {}) {
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

    protected open fun createTitle(): String {
        return title
    }

    /**
     * 构建页面
     */
    override fun build(): Inventory {
        val inventory = Bukkit.createInventory(holderCallback(this), if (rows > 0) rows * 9 else slots.size * 9, createTitle())
        var row = 0
        while (row < slots.size) {
            val line = slots[row]
            var cel = 0
            while (cel < line.size && cel < 9) {
                inventory.setItem(row * 9 + cel, items[line[cel]] ?: ItemStack(Material.AIR))
                cel++
            }
            row++
        }
        return inventory
    }
}