package taboolib.module.ui.receptacle.operates

import taboolib.module.ui.receptacle.ReceptacleType

/**
 * @author Arasple
 * @date 2020/12/4 21:22
 *
 * This is sent to the client when it should open an inventory,
 * such as a chest, workbench, or furnace.
 * This message is not sent anywhere for clients opening their own inventory. For horses, use Open Horse Window.
 *
 * A unique id number for the window to be displayed. Notchian server implementation is a counter, starting at 1.
 *
 * @param type The window type to use for display. See ReceptacleType for the different values.
 * @param title The title of the window
 */
class OperateWindowOpen(val type: ReceptacleType, val title: String, override val packet: Boolean = true) : OperateInventory() {

    val windowId: Int = if (packet) 119 else 120
}