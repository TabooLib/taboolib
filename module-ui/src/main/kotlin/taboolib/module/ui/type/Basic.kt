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

class Basic(title: String = "chest") : Menu(title) {

    internal var rows = -1
    internal var handLocked = true
    internal var items = HashMap<Char, ItemStack>()
    internal var slots = ArrayList<List<Char>>()

    internal var onClick: ((event: ClickEvent) -> Unit) = {}
    internal var onClose: ((event: InventoryCloseEvent) -> Unit) = {}
    internal var onBuild: ((player: Player, inventory: Inventory) -> Unit) = { _, _ -> }
    internal var onBuildAsync: ((player: Player, inventory: Inventory) -> Unit) = { _, _ -> }

    fun rows(rows: Int) {
        this.rows = rows
    }

    /**
     * 设置是否锁定玩家手部动作
     * 设置为 true 则将阻止玩家在使用菜单时进行包括但不限于
     * 丢弃物品，拿出菜单物品等行为
     *
     * @param handLocked 锁定
     */
    fun handLocked(handLocked: Boolean) {
        this.handLocked = handLocked
    }

    fun onBuild(async: Boolean = false, onBuild: (player: Player, inventory: Inventory) -> Unit) {
        if (async) {
            this.onBuildAsync = onBuild
        } else {
            this.onBuild = onBuild
        }
    }

    fun onClose(onClose: (event: InventoryCloseEvent) -> Unit) {
        this.onClose = onClose
    }

    fun onClick(bind: Int, onClick: (event: ClickEvent) -> Unit = {}) {
        val e = this.onClick
        onClick(lock = true) {
            if (it.rawSlot == bind) {
                onClick(it)
            } else {
                e(it)
            }
        }
    }

    fun onClick(bind: Char, onClick: (event: ClickEvent) -> Unit = {}) {
        val e = this.onClick
        onClick(lock = true) {
            if (it.slot == bind) {
                onClick(it)
            } else {
                e(it)
            }
        }
    }

    fun onClick(lock: Boolean = false, onClick: (event: ClickEvent) -> Unit = {}) {
        if (lock) {
            this.onClick = {
                it.isCancelled = true
                if (it.clickType == ClickType.CLICK) {
                    onClick(it)
                }
            }
        } else {
            this.onClick = onClick
        }
    }

    fun map(vararg slots: String) {
        this.slots.clear()
        this.slots.addAll(slots.map { it.toCharArray().toList() })
    }

    fun set(slot: Char, itemStack: ItemStack) {
        items[slot] = itemStack
    }

    fun set(slot: Char, material: XMaterial, itemBuilder: ItemBuilder.() -> Unit) {
        set(slot, buildItem(material, itemBuilder))
    }

    fun set(slot: Int, material: XMaterial, itemBuilder: ItemBuilder.() -> Unit) {
        set(slot, buildItem(material, itemBuilder))
    }

    fun set(slot: Int, itemStack: ItemStack) {
        val e = this.onBuild
        onBuild { player, it ->
            it.setItem(slot, itemStack)
            e(player, it)
        }
    }

    fun getSlot(slot: Int): Char {
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

    override fun build(): Inventory {
        val inventory = Bukkit.createInventory(MenuHolder(this), if (rows > 0) rows * 9 else slots.size * 9, title)
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