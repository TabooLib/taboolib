package taboolib.module.nms.type

import org.bukkit.Material

/**
 * [Toast] 信息
 * 
 * @param material 物品材质
 * @param message 消息
 * @param frame 框架
 */
data class Toast(val material: Material, val message: String, val frame: ToastFrame)