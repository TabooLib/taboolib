package taboolib.module.ui.receptacle.operates

/**
 * @author Arasple
 * @date 2020/12/4 21:54
 * https://wiki.vg/Protocol#Window_Property
 *
 * TODO This packet is used to inform the client that part of a GUI window should be updated.
 */
class OperateWindowUpdateData(val property: Int, val value: Int, override val packet: Boolean = true) : OperateInventory() {

    val windowId: Int = if (packet) 119 else 120
}