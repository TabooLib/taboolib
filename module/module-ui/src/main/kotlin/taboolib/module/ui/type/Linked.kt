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
import taboolib.module.ui.MenuHolder
import taboolib.module.ui.buildMenu
import taboolib.platform.util.isNotAir

@Isolated
open class Linked<T>(title: String) : Menu(title) {

    var page = 0
        private set

    private var rows = 1
    private var handLocked = true
    private val button = HashMap<Int, ClickEvent.() -> Unit>()
    private val menuSlots = ArrayList<Int>()
    private var menuElements: (() -> List<T>) = { emptyList() }
    private var menuElementsCache = emptyList<T>()
    private var onClick: ((event: ClickEvent, element: T) -> Unit) = { _, _ -> }
    private var onClickGeneral: ((event: ClickEvent) -> Unit) = {}
    private var onClose: ((event: InventoryCloseEvent) -> Unit) = {}
    private var onBuild: ((inventory: Inventory) -> Unit) = {}
    private var onBuildAsync: ((inventory: Inventory) -> Unit) = {}
    private var onGenerate: ((player: Player, element: T, index: Int, slot: Int) -> ItemStack) = { _, _, _, _ -> ItemStack(Material.AIR) }
    private var onGenerateAsync: ((player: Player, element: T, index: Int, slot: Int) -> ItemStack) = { _, _, _, _ -> ItemStack(Material.AIR) }
    private var holder: ((menu: Basic) -> MenuHolder) = { MenuHolder(it) }

    private lateinit var player: Player

    fun page(page: Int) {
        this.page = page
    }

    fun rows(rows: Int) {
        this.rows = rows
    }

    fun handLocked(handLocked: Boolean) {
        this.handLocked = handLocked
    }

    fun holder(func: (menu: Basic) -> MenuHolder) {
        this.holder = func
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
            val e = this.onBuildAsync
            this.onBuildAsync = { inventory ->
                onBuild(inventory)
                e(inventory)
            }
        } else {
            val e = this.onBuild
            this.onBuild = { inventory ->
                onBuild(inventory)
                e(inventory)
            }
        }
    }

    fun onClose(onClose: (event: InventoryCloseEvent) -> Unit) {
        this.onClose = onClose
    }

    fun onClick(onClick: (event: ClickEvent, element: T) -> Unit) {
        this.onClick = onClick
    }

    fun onClick(onClick: (event: ClickEvent) -> Unit) {
        this.onClickGeneral = onClick
    }

    fun set(slot: Int, itemStack: ItemStack, onClick: ClickEvent.() -> Unit = {}) {
        button[slot] = onClick
        onBuild {
            it.setItem(slot, itemStack)
        }
    }

    fun setNextPage(slot: Int, onGenerate: (page: Int, hasNextPage: Boolean) -> ItemStack) {
        button[slot] = {
            if (hasNextPage()) {
                page++
                player.openInventory(build())
            }
        }
        onBuild {
            it.setItem(slot, onGenerate(page, hasNextPage()))
        }
    }

    fun setPreviousPage(slot: Int, onGenerate: (page: Int, hasPreviousPage: Boolean) -> ItemStack) {
        button[slot] = {
            if (hasPreviousPage()) {
                page--
                player.openInventory(build())
            }
        }
        onBuild {
            it.setItem(slot, onGenerate(page, hasPreviousPage()))
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
        return buildMenu<Basic>(title.replace("%p", (page + 1).toString())) {
            holder(this@Linked.holder)
            handLocked(this@Linked.handLocked)
            rows(this@Linked.rows)
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
                this@Linked.onBuild(it)
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
                this@Linked.onBuildAsync(it)
            }
            onClick(lock = true) {
                if (objectsMap.containsKey(it.rawSlot)) {
                    this@Linked.onClick(it, objectsMap[it.rawSlot]!!)
                } else if (button.containsKey(it.rawSlot)) {
                    button[it.rawSlot]!!(it)
                } else {
                    onClickGeneral(it)
                }
            }
            onClose {
                this@Linked.onClose(it)
            }
        }
    }

    private fun isNext(page: Int, size: Int, entry: Int): Boolean {
        return size / entry.toDouble() > page + 1
    }
}