package taboolib.platform.util

import org.bukkit.inventory.ItemStack
import taboolib.module.chat.TellrawJson
import taboolib.module.nms.getInternalKey
import taboolib.module.nms.getItemTag

fun TellrawJson.hoverItem(itemStack: ItemStack): TellrawJson {
    return hoverItem(itemStack.getInternalKey(), itemStack.amount, itemStack.getItemTag().toString())
}