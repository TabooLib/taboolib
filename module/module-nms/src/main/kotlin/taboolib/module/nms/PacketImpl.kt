package taboolib.module.nms

import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.Reflex.Companion.setProperty

/**
 * TabooLib
 * taboolib.module.nms.PacketImpl
 *
 * @author 坏黑
 * @since 2023/2/2 17:57
 */
class PacketImpl(override var source: Any) : Packet() {

    override var name = source.javaClass.simpleName.toString()

    override var fullyName = source.javaClass.name.toString()

    override fun <T> read(name: String): T? {
        return source.getProperty<T>(name)
    }

    override fun write(name: String, value: Any?) {
        source.setProperty(name, value)
    }

    override fun overwrite(newPacket: Any) {
        source = newPacket
        name = newPacket.javaClass.simpleName.toString()
        fullyName = newPacket.javaClass.name.toString()
    }
}