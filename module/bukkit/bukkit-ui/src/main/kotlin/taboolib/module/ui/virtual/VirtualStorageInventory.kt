package taboolib.module.ui.virtual

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.HumanEntity
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

/**
 * TabooLib
 * taboolib.module.ui.virtual.VirtualStorageInventory
 *
 * @author 坏黑
 * @since 2023/1/16 14:16
 */
class VirtualStorageInventory(val sourceInventory: VirtualInventory) : Inventory {

    val delegateInventory: Inventory

    init {
        sourceInventory.initStorageItems()
        delegateInventory = Bukkit.createInventory(null, 36)
        delegateInventory.contents = sourceInventory.storageContents!!.toTypedArray()
    }

    override fun iterator(): MutableListIterator<ItemStack> {
        return delegateInventory.iterator()
    }

    override fun iterator(p0: Int): MutableListIterator<ItemStack> {
        return delegateInventory.iterator(p0)
    }

    override fun getSize(): Int {
        return 36
    }

    override fun getMaxStackSize(): Int {
        return delegateInventory.maxStackSize
    }

    override fun setMaxStackSize(p0: Int) {
        delegateInventory.maxStackSize = p0
    }

    override fun getItem(p0: Int): ItemStack {
        return sourceInventory.getStorageItem(p0)
    }

    override fun setItem(p0: Int, p1: ItemStack?) {
        sourceInventory.setStorageItem(p0, p1)
        delegateInventory.setItem(p0, p1)
    }

    override fun addItem(vararg p0: ItemStack?): HashMap<Int, ItemStack> {
        return delegateInventory.addItem(*p0)
    }

    override fun removeItem(vararg p0: ItemStack?): HashMap<Int, ItemStack> {
        return delegateInventory.removeItem(*p0)
    }

    override fun getContents(): Array<ItemStack> {
        return delegateInventory.contents
    }

    override fun setContents(p0: Array<out ItemStack>) {
        sourceInventory.setStorageItems(p0.toList())
        delegateInventory.contents = p0
    }

    override fun getStorageContents(): Array<ItemStack> {
        return contents
    }

    override fun setStorageContents(p0: Array<out ItemStack>) {
        setContents(p0)
    }

    override fun contains(p0: Material): Boolean {
        return delegateInventory.contains(p0)
    }

    override fun contains(p0: ItemStack?): Boolean {
        return delegateInventory.contains(p0)
    }

    override fun contains(p0: Material, p1: Int): Boolean {
        return delegateInventory.contains(p0, p1)
    }

    override fun contains(p0: ItemStack?, p1: Int): Boolean {
        return delegateInventory.contains(p0, p1)
    }

    override fun containsAtLeast(p0: ItemStack?, p1: Int): Boolean {
        return delegateInventory.containsAtLeast(p0, p1)
    }

    override fun all(p0: Material): HashMap<Int, out ItemStack> {
        return delegateInventory.all(p0)
    }

    override fun all(p0: ItemStack?): HashMap<Int, out ItemStack> {
        return delegateInventory.all(p0)
    }

    override fun first(p0: Material): Int {
        return delegateInventory.first(p0)
    }

    override fun first(p0: ItemStack): Int {
        return delegateInventory.first(p0)
    }

    override fun firstEmpty(): Int {
        return delegateInventory.firstEmpty()
    }

    override fun isEmpty(): Boolean {
        return delegateInventory.isEmpty
    }

    override fun remove(p0: Material) {
        delegateInventory.remove(p0)
    }

    override fun remove(p0: ItemStack) {
        delegateInventory.remove(p0)
    }

    override fun clear(p0: Int) {
        delegateInventory.clear(p0)
    }

    override fun clear() {
        delegateInventory.clear()
    }

    override fun getViewers(): MutableList<HumanEntity> {
        return arrayListOf()
    }

    override fun getType(): InventoryType {
        return InventoryType.CHEST
    }

    override fun getHolder(): InventoryHolder? {
        return null
    }

    override fun getLocation(): Location? {
        return null
    }
}