package taboolib.module.ui.type

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.common.Isolated
import taboolib.common.util.subList
import taboolib.module.ui.ClickEvent
import taboolib.module.ui.Menu
import taboolib.module.ui.buildMenu
import taboolib.platform.util.isNotAir

@Isolated
class Linked<T>(title: String) : Menu(title) {

    private var rows = 1
    private var handLocked = true
    private val button = HashMap<Int, ClickEvent.() -> Unit>()
    private val menuSlots = ArrayList<Int>()
    private var menuElements: (() -> List<T>) = { emptyList() }
    private var menuElementsCache = emptyList<T>()
    private var page = 0
    private var onClick: ((event: ClickEvent, element: T) -> Unit) = { _, _ -> }
    private var onClose: ((event: InventoryCloseEvent) -> Unit) = {}
    private var onBuild: ((inventory: Inventory) -> Unit) = {}
    private var onBuildAsync: ((inventory: Inventory) -> Unit) = {}
    private var onGenerate: ((player: Player, element: T, index: Int, slot: Int) -> ItemStack) = { _, _, _, _ -> ItemStack(Material.AIR) }
    private var onGenerateAsync: ((player: Player, element: T, index: Int, slot: Int) -> ItemStack) = { _, _, _, _ -> ItemStack(Material.AIR) }

    private lateinit var player: Player

    fun rows(rows: Int) {
        this.rows = rows
    }

    fun handLocked(handLocked: Boolean) {
        this.handLocked = handLocked
    }

    fun slots(slots: List<Int>) {
        this.menuSlots.clear()
        this.menuSlots += slots
    }

    fun elements(elements: () -> List<T>) {
        this.menuElements = elements
    }

    fun onGenerate(async: Boolean = false, onGenerate: (player: Player, element: T, index: Int, slot: Int) -> ItemStack) {
        if (async) {
            this.onGenerateAsync = onGenerate
        } else {
            this.onGenerate = onGenerate
        }
    }

    fun onBuild(async: Boolean = false, onBuild: (inventory: Inventory) -> Unit) {
        if (async) {
            this.onBuildAsync = onBuild
        } else {
            this.onBuild = onBuild
        }
    }

    fun onClose(onClose: (event: InventoryCloseEvent) -> Unit) {
        this.onClose = onClose
    }

    fun onClick(onClick: (event: ClickEvent, element: T) -> Unit) {
        this.onClick = onClick
    }

    fun set(slot: Int, itemStack: ItemStack, onClick: ClickEvent.() -> Unit = {}) {
        button[slot] = onClick
        val e = onBuild
        onBuild {
            it.setItem(slot, itemStack)
            e(it)
        }
    }

    fun setNextPage(slot: Int, onGenerate: (page: Int, hasNextPage: Boolean) -> ItemStack) {
        button[slot] = {
            if (hasNextPage()) {
                page++
                player.openInventory(build())
            }
        }
        val e = onBuild
        onBuild {
            it.setItem(slot, onGenerate(page, hasNextPage()))
            e(it)
        }
    }

    fun setPreviousPage(slot: Int, onGenerate: (page: Int, hasPreviousPage: Boolean) -> ItemStack) {
        button[slot] = {
            if (hasPreviousPage()) {
                page--
                player.openInventory(build())
            }
        }
        val e = onBuild
        onBuild {
            it.setItem(slot, onGenerate(page, hasPreviousPage()))
            e(it)
        }
    }

    fun hasPreviousPage(): Boolean {
        return page > 0
    }

    fun hasNextPage(): Boolean {
        return isNext(page, menuElementsCache.size, menuSlots.size)
    }

    override fun build(): Inventory {
        menuElementsCache = menuElements()
        val objectsMap = HashMap<Int, T>()
        val items = subList(menuElementsCache, page * menuSlots.size, (page + 1) * menuSlots.size)
        return buildMenu<Basic>(title) {
            handLocked(handLocked)
            rows(rows)
            onBuild { p, it ->
                player = p
                items.forEachIndexed { index, item ->
                    val slot = menuSlots.getOrNull(index) ?: 0
                    objectsMap[slot] = item
                    val itemStack = onGenerate(player, item, index, slot)
                    if (itemStack.isNotAir()) {
                        it.setItem(slot, itemStack)
                    }
                }
                onBuild(it)
            }
            onBuild(true) { p, it ->
                player = p
                items.forEachIndexed { index, item ->
                    val slot = menuSlots.getOrNull(index) ?: 0
                    objectsMap[slot] = item
                    val itemStack = onGenerateAsync(player, item, index, slot)
                    if (itemStack.isNotAir()) {
                        it.setItem(slot, itemStack)
                    }
                }
                onBuildAsync(it)
            }
            onClick {
                if (objectsMap.containsKey(it.rawSlot)) {
                    onClick(it, objectsMap[it.rawSlot]!!)
                } else if (button.containsKey(it.rawSlot)) {
                    button[it.rawSlot]!!(it)
                }
            }
            onClose {
                onClose(it)
            }
        }
    }

    private fun isNext(page: Int, size: Int, entry: Int): Boolean {
        return size / entry.toDouble() > page + 1
    }
}