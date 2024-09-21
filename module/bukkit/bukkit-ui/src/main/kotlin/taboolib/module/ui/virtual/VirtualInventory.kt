package taboolib.module.ui.virtual

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.HumanEntity
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

/**
 * TabooLib
 * taboolib.module.ui.virtual.VirtualInventory
 *
 * @author 坏黑
 * @since 2023/1/16 01:16
 */
class VirtualInventory(val bukkitInventory: Inventory, storageContents: List<ItemStack>? = null) : Inventory {

    /** 远程背包实例 */
    var remoteInventory: RemoteInventory? = null

    /** 玩家背包内容 */
    var storageContents: List<ItemStack>? = storageContents
        private set(value) {
            // 如果不满 36 则补齐
            field = if (value != null && value.size < 36) {
                value + List(36 - value.size) { ItemStack(Material.AIR) }
            } else {
                value
            }
        }

    init {
        // 重复包装检查
        if (bukkitInventory is VirtualInventory) {
            error("VirtualInventory can not be wrapped again.")
        }
    }

    /** 初始化玩家背包内容 */
    fun initStorageItems() {
        if (storageContents == null) {
            // 初始化玩家玩家部分
            if (remoteInventory == null) {
                error("VirtualInventory is not opened or storageContents is not set.")
            }
            storageContents = remoteInventory!!.viewer.getStorageItems().map { it ?: ItemStack(Material.AIR) }
        }
    }

    /** 获取玩家背包内容 */
    fun getStorageItem(slot: Int): ItemStack {
        if (storageContents == null) {
            initStorageItems()
        }
        return storageContents!![slot]
    }

    /** 获取玩家背包内容 */
    fun getStorageItems(): List<ItemStack> {
        if (storageContents == null) {
            initStorageItems()
        }
        return storageContents!!
    }

    /** 设置玩家背包内容 */
    fun setStorageItem(slot: Int, item: ItemStack?) {
        if (storageContents == null) {
            initStorageItems()
        }
        val newStorageContents = storageContents!!.toMutableList()
        newStorageContents[slot] = item ?: ItemStack(Material.AIR)
        storageContents = newStorageContents
        remoteInventory?.sendSlotChange(size + slot, item ?: ItemStack(Material.AIR))
    }

    /** 设置玩家背包内容 */
    fun setStorageItems(items: List<ItemStack>) {
        storageContents = items
        remoteInventory?.refresh(bukkitInventory.contents.map { it ?: ItemStack(Material.AIR) }, storageContents)
    }

    override fun iterator(): MutableListIterator<ItemStack> {
        return bukkitInventory.iterator()
    }

    override fun iterator(p0: Int): MutableListIterator<ItemStack> {
        return bukkitInventory.iterator(p0)
    }

    override fun getSize(): Int {
        return bukkitInventory.size
    }

    override fun getMaxStackSize(): Int {
        return bukkitInventory.maxStackSize
    }

    override fun setMaxStackSize(p0: Int) {
        bukkitInventory.maxStackSize = p0
    }

    override fun getItem(p0: Int): ItemStack? {
        return bukkitInventory.getItem(p0)
    }

    override fun setItem(slot: Int, item: ItemStack?) {
        bukkitInventory.setItem(slot, item)
        remoteInventory?.sendSlotChange(slot, item ?: ItemStack(Material.AIR))
    }

    override fun addItem(vararg p0: ItemStack?): HashMap<Int, ItemStack> {
        return bukkitInventory.addItem(*p0)
    }

    override fun removeItem(vararg p0: ItemStack?): HashMap<Int, ItemStack> {
        return bukkitInventory.removeItem(*p0)
    }

    override fun getContents(): Array<ItemStack?> {
        return bukkitInventory.contents
    }

    override fun setContents(p0: Array<ItemStack?>) {
        bukkitInventory.contents = p0
        remoteInventory?.refresh(bukkitInventory.contents.map { it ?: ItemStack(Material.AIR) }, storageContents)
    }

    override fun getStorageContents(): Array<ItemStack> {
        return getStorageItems().toTypedArray()
    }

    override fun setStorageContents(p0: Array<ItemStack>) {
        setStorageItems(p0.toList())
    }

    override fun contains(p0: Material): Boolean {
        return bukkitInventory.contains(p0)
    }

    override fun contains(p0: ItemStack?): Boolean {
        return bukkitInventory.contains(p0)
    }

    override fun contains(p0: Material, p1: Int): Boolean {
        return bukkitInventory.contains(p0, p1)
    }

    override fun contains(p0: ItemStack?, p1: Int): Boolean {
        return bukkitInventory.contains(p0, p1)
    }

    override fun containsAtLeast(p0: ItemStack?, p1: Int): Boolean {
        return bukkitInventory.containsAtLeast(p0, p1)
    }

    override fun all(p0: Material): HashMap<Int, out ItemStack> {
        return bukkitInventory.all(p0)
    }

    override fun all(p0: ItemStack?): HashMap<Int, out ItemStack> {
        return bukkitInventory.all(p0)
    }

    override fun first(p0: Material): Int {
        return bukkitInventory.first(p0)
    }

    override fun first(p0: ItemStack): Int {
        return bukkitInventory.first(p0)
    }

    override fun firstEmpty(): Int {
        return bukkitInventory.firstEmpty()
    }

    override fun isEmpty(): Boolean {
        return bukkitInventory.isEmpty
    }

    override fun remove(p0: Material) {
        bukkitInventory.remove(p0)
    }

    override fun remove(p0: ItemStack) {
        bukkitInventory.remove(p0)
    }

    override fun clear(p0: Int) {
        bukkitInventory.clear(p0)
    }

    override fun clear() {
        bukkitInventory.clear()
    }

    override fun getViewers(): MutableList<HumanEntity> {
        return remoteInventory?.viewer?.let { mutableListOf(it) } ?: mutableListOf()
    }

    override fun getType(): InventoryType {
        return bukkitInventory.type
    }

    override fun getHolder(): InventoryHolder? {
        return bukkitInventory.holder
    }

    override fun getLocation(): Location? {
        return bukkitInventory.location
    }

    /**
     * 对于老版本, Inventory 下有 getTitle 函数
     * 部分插件监听 InventoryCloseEvent 时调用, 所以得给个标题给他们玩
     */
    fun getTitle(): String {
        return remoteInventory?.title ?: ""
    }

}