package taboolib.module.nms

/**
 * TabooLib
 * taboolib.module.nms.Packet
 *
 * @author sky
 * @since 2021/6/24 5:39 下午
 */
abstract class Packet {

    /** 原始数据包 */
    abstract val source: Any

    /** 数据包名称 */
    abstract val name: String

    /** 数据包名称（强制 Spigot 译名）*/
    abstract val nameInSpigot: String?

    /** 数据包完整名称 */
    abstract val fullyName: String

    /** 读取字段 */
    abstract fun <T> read(name: String, remap: Boolean = true): T?

    /** 写入字段 */
    abstract fun write(name: String, value: Any?, remap: Boolean = true)

    /** 覆盖原始数据包 */
    abstract fun overwrite(newPacket: Any)

    override fun toString(): String {
        return name
    }
}