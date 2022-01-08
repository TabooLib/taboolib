package taboolib.module.ui.receptacle

import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.inventory.InventoryType.*
import java.util.*

private val viewingReceptacleMap = HashMap<UUID, Receptacle>()

fun buildReceptacle(title: String, row: Int = 1, builder: ChestInventory.() -> Unit): ChestInventory {
    return ChestInventory(row, title).also(builder)
}

fun buildReceptacle(title: String, type: ReceptacleType = ReceptacleType.GENERIC_9X1, packet: Boolean = true, builder: Receptacle.() -> Unit): Receptacle {
    return Receptacle(type, title, packet).also(builder)
}

fun Player.openReceptacle(title: String, row: Int = 1, builder: ChestInventory.() -> Unit) {
    buildReceptacle(title, row, builder).open(this)
}

fun Player.openReceptacle(title: String, type: ReceptacleType = ReceptacleType.GENERIC_9X1, packet: Boolean = true, builder: Receptacle.() -> Unit) {
    buildReceptacle(title, type, packet, builder).open(this)
}

fun Player.getViewingReceptacle(): Receptacle? {
    return viewingReceptacleMap[uniqueId]
}

fun Player.setViewingReceptacle(receptacle: Receptacle?) {
    if (receptacle == null) {
        viewingReceptacleMap.remove(uniqueId)
    } else {
        viewingReceptacleMap[uniqueId] = receptacle
    }
}

fun InventoryType.createReceptacle(title: String = defaultTitle, packet: Boolean = true): Receptacle {
    if (this != CHEST) {
        val receptacleType = when (this.name) {
            "ENDER_CHEST", "BARREL" -> ReceptacleType.GENERIC_9X3
            "DISPENSER", "DROPPER" -> ReceptacleType.GENERIC_3X3
            "ANVIL" -> ReceptacleType.ANVIL
            "FURNACE" -> ReceptacleType.FURNACE
            "WORKBENCH", "CRAFTING" -> ReceptacleType.CRAFTING
            "ENCHANTING" -> ReceptacleType.ENCHANTMENT_TABLE
            "BREWING" -> ReceptacleType.BREWING_STAND
            "MERCHANT" -> ReceptacleType.MERCHANT
            "BEACON" -> ReceptacleType.BEACON
            "HOPPER" -> ReceptacleType.HOPPER
            "SHULKER_BOX" -> ReceptacleType.SHULKER_BOX
            "BLAST_FURNACE" -> ReceptacleType.BLAST_FURNACE
            "SMOKER" -> ReceptacleType.SMOKER
            "LOOM" -> ReceptacleType.LOOM
            "CARTOGRAPHY" -> ReceptacleType.CARTOGRAPHY
            "GRINDSTONE" -> ReceptacleType.GRINDSTONE
            "STONECUTTER" -> ReceptacleType.STONE_CUTTER
            else -> throw IllegalArgumentException("Unsupported $this")
        }
        return Receptacle(receptacleType, title, packet)
    }
    return ChestInventory()
}