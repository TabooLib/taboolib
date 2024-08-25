package taboolib.platform.util

import org.bukkit.inventory.ItemStack
import taboolib.library.xseries.XMaterial

/**
 * 判定材质是否为空气
 */
val XMaterial.isAir get() = this == XMaterial.AIR || this == XMaterial.CAVE_AIR || this == XMaterial.VOID_AIR

/**
 * 判定物品是否为空气
 */
val ItemStack?.isAir get() = isAir()