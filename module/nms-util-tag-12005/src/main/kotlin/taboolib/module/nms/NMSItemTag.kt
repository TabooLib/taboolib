package taboolib.module.nms

import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.item.component.CustomData
import org.bukkit.craftbukkit.v1_20_R4.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack

/**
 * [NMSItemTag] 的实现类
 */
class NMSItemTagImpl2 : NMSItemTagImpl1() {

    private fun getNMSCopy(itemStack: ItemStack): net.minecraft.world.item.ItemStack {
        return CraftItemStack.asNMSCopy(itemStack)
    }

    private fun getBukkitCopy(itemStack: net.minecraft.world.item.ItemStack): ItemStack {
        return CraftItemStack.asBukkitCopy(itemStack)
    }

    override fun getItemTag(itemStack: ItemStack): ItemTag {
        val nmsItem = getNMSCopy(itemStack)
        val tag = nmsItem.get(DataComponents.CUSTOM_DATA)?.copyTag()
        return if (tag != null) itemTagToBukkitCopy(tag).asCompound() else ItemTag()
    }

    override fun setItemTag(itemStack: ItemStack, itemTag: ItemTag): ItemStack {
        val nmsItem = getNMSCopy(itemStack)
        nmsItem.set(DataComponents.CUSTOM_DATA, CustomData.of(itemTagToNMSCopy(itemTag) as NBTTagCompound))
        return getBukkitCopy(nmsItem)
    }
}