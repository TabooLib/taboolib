package taboolib.module.ui.type

import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.library.xseries.XMaterial
import taboolib.module.ui.ClickEvent
import taboolib.module.ui.Menu
import taboolib.module.ui.MenuHolder
import taboolib.platform.util.ItemBuilder
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 标准容器
 */
interface Chest : Menu {

    /** 获取行数 */
    val rows: Int

    /** 是否正在使用虚拟化 */
    val virtualized: Boolean

    /** 虚拟化时玩家背包内容 */
    val virtualizedStorageContents: List<ItemStack>?

    /** 物品与对应抽象字符关系 **/
    val items: ConcurrentHashMap<Char, ItemStack>

    /** 抽象字符布局 **/
    val slots: CopyOnWriteArrayList<List<Char>>

    /** 是否锁定主手 **/
    val handLocked: Boolean

    /** 是否打开过 **/
    val isOpened: Boolean

    /**
     * 启用虚拟化页面（将自动阻止所有点击行为）
     */
    fun virtualize(storageContents: List<ItemStack>? = null)

    /**
     * 隐藏玩家背包（自动启动虚拟页面）
     */
    fun hidePlayerInventory()

    /**
     * 行数
     * 为 1 - 6 之间的整数，并非原版 9 的倍数
     */
    fun rows(rows: Int)

    /**
     * 设置是否锁定玩家手部动作
     * 设置为 true 则将阻止玩家在使用菜单时进行包括但不限于
     * 丢弃物品，拿出菜单物品等行为
     *
     * @param handLocked 锁定
     */
    fun handLocked(handLocked: Boolean)

    /**
     * 设置 MenuHolder 创建回调
     */
    fun holder(func: (menu: Chest) -> MenuHolder)

    /**
     * 页面构建时触发回调
     * 可选是否异步执行
     */
    fun onBuild(async: Boolean = false, callback: (player: Player, inventory: Inventory) -> Unit)

    /**
     * 页面关闭时触发回调
     * 只能触发一次（玩家客户端强制关闭时会触发两次原版 InventoryCloseEvent 事件）
     *
     * TODO 2023/10/09 若启用虚拟化菜单，则 player.closeInventory() 不会触发该回调函数
     */
    fun onClose(once: Boolean = true, skipUpdateTitle: Boolean = true, callback: (event: InventoryCloseEvent) -> Unit)

    /**
     * 点击事件回调
     * 仅在特定位置下触发
     */
    fun onClick(bind: Int, callback: (event: ClickEvent) -> Unit = {})

    /**
     * 点击事件回调
     * 仅在特定位置下触发
     */
    fun onClick(bind: Char, callback: (event: ClickEvent) -> Unit = {})

    /**
     * 整页点击事件回调
     * 可选是否自动锁定点击位置
     */
    fun onClick(lock: Boolean = false, callback: (event: ClickEvent) -> Unit = {})

    /**
     * 使用抽象字符页面布局
     */
    fun map(vararg slots: String)

    /**
     * 根据抽象符号设置物品
     */
    fun set(slot: Char, itemStack: ItemStack)

    /**
     * 根据位置设置物品
     */
    fun set(slot: Int, itemStack: ItemStack)

    /**
     * 根据抽象符号设置物品
     */
    fun set(slot: Char, callback: () -> ItemStack)

    /**
     * 根据位置设置物品
     */
    fun set(slot: Int, callback: () -> ItemStack)

    /**
     * 根据抽象符号设置物品
     */
    fun set(slot: Char, material: XMaterial, itemBuilder: ItemBuilder.() -> Unit = {})

    /**
     * 根据位置设置物品
     */
    fun set(slot: Int, material: XMaterial, itemBuilder: ItemBuilder.() -> Unit = {})

    /**
     * 根据抽象符号设置物品
     */
    fun set(slot: Char, itemStack: ItemStack, onClick: ClickEvent.() -> Unit = {})

    /**
     * 根据位置设置物品
     */
    fun set(slot: Int, itemStack: ItemStack, onClick: ClickEvent.() -> Unit = {})

    /**
     * 获取位置对应的抽象字符
     */
    fun getSlot(slot: Int): Char

    /**
     * 获取抽象字符对应的位置
     */
    fun getSlots(slot: Char): List<Int>

    /**
     * 获取抽象字符对应的首个位置
     */
    fun getFirstSlot(slot: Char): Int

    /**
     * 更新标题
     */
    fun updateTitle(title: String)
}