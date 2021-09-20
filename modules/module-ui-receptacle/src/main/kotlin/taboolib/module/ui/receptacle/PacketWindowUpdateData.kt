package taboolib.module.ui.receptacle

/**
 * @author Arasple
 * @date 2020/12/4 21:54
 * https://wiki.vg/Protocol#Window_Property
 *
 * TODO This packet is used to inform the client that part of a GUI window should be updated.
 */
class PacketWindowUpdateData(val property: Int, val value: Int) : PacketInventory {

    val windowId: Int = 119
}