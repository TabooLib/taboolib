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

    /** 数据包名称 */
    override var name = source.javaClass.simpleName.toString()

    /** 数据包完整名称 */
    override var fullyName = source.javaClass.name.toString()

    /** 读取字段 */
    override fun <T> read(name: String, remap: Boolean): T? {
        return source.getProperty<T>(name, remap = remap)
    }

    /** 写入字段 */
    override fun write(name: String, value: Any?) {
        source.setProperty(name, value)
    }

    /** 覆盖原始数据包 */
    override fun overwrite(newPacket: Any) {
        source = newPacket
        name = newPacket.javaClass.simpleName.toString()
        fullyName = newPacket.javaClass.name.toString()
    }
}