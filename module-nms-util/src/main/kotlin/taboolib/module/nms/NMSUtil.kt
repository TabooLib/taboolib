package taboolib.module.nms

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

val nmsUtil = nmsProxy(NMS::class.java, "{name}Impl")

fun ItemStack.getItemTag(): ItemTag {
    return nmsUtil.getItemTag(this)
}

fun ItemStack.setItemTag(itemTag: ItemTag): ItemStack {
    return nmsUtil.setItemTag(this, itemTag)
}

fun ItemStack.getInternalName(): String {
    return nmsUtil.getName(this)
}

fun Entity.getInternalName(): String {
    return nmsUtil.getName(this)
}

fun Player.sendPacket(packet: Any) {
    nmsUtil.sendPacket(this, packet)
}