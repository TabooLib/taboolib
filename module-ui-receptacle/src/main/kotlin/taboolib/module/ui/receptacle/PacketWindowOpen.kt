package taboolib.module.ui.receptacle

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
class PacketWindowOpen(val type: ReceptacleType, val title: String) : PacketInventory {

    val windowId = 119
}