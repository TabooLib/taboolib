@file:Isolated
package taboolib.module.ui

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.common.Isolated
import taboolib.module.ui.type.Linked
import taboolib.module.ui.type.Stored
import taboolib.platform.util.giveItem
import taboolib.platform.util.inventoryCenterSlots
import taboolib.platform.util.isNotAir

fun build(player: Player) {
    player.openMenu<Linked<Material>> {
        slots(inventoryCenterSlots)
        elements {
            Material.values().toList()
        }
        onGenerate { player, element, index, slot ->
            ItemStack(element)
        }
        onClick { event, element ->
            player.giveItem(ItemStack(element))
        }
    }
    player.openMenu<Stored> {
        rule {
            checkSlot { inventory, itemStack, slot ->
                false
            }
            firstSlot { inventory, itemStack ->
                -1
            }
            writeItem { inventory, itemStack, slot ->

            }
            readItem { inventory, slot ->
                inventory.getItem(slot)
            }
        }
    }
}

inline fun <reified T : Menu> buildMenu(title: String = "chest", builder: T.() -> Unit): Inventory {
    return T::class.java.getDeclaredConstructor(String::class.java).newInstance(title).also(builder).build()
}

inline fun <reified T : Menu> Player.openMenu(title: String = "chest", builder: T.() -> Unit) {
    openInventory(buildMenu(title, builder))
}

fun InventoryClickEvent.getAffectItems(): List<ItemStack> {
    val items = ArrayList<ItemStack>()
    if (click == ClickType.NUMBER_KEY) {
        val hotbarButton = whoClicked.inventory.getItem(hotbarButton)
        if (hotbarButton.isNotAir()) {
            items += hotbarButton!!
        }
    }
    if (currentItem.isNotAir()) {
        items += currentItem!!
    }
    return items
}