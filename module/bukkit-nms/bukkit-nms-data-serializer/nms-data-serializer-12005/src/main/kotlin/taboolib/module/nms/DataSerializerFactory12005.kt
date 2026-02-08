package taboolib.module.nms

import io.netty.buffer.ByteBufOutputStream
import io.netty.buffer.Unpooled
import net.minecraft.core.IRegistryCustom
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.ComponentSerialization
import org.bukkit.craftbukkit.v1_21_R1.util.CraftChatMessage
import java.io.DataOutput

/**
 * Adyeshach
 * taboolib.module.nms.DataSerializerFactoryImpl
 *
 * @author 坏黑
 * @since 2022/12/12 23:30
 */
class DataSerializerFactory12005 : DataSerializerFactory, DataSerializer {

    val buf: RegistryFriendlyByteBuf = RegistryFriendlyByteBuf(Unpooled.buffer(), IRegistryCustom.EMPTY)

    override fun writeByte(byte: Byte): DataSerializer {
        return buf.writeByte(byte.toInt()).let { this }
    }

    override fun writeBytes(bytes: ByteArray): DataSerializer {
        return buf.writeBytes(bytes).let { this }
    }

    override fun writeShort(short: Short): DataSerializer {
        return buf.writeShort(short.toInt()).let { this }
    }

    override fun writeInt(int: Int): DataSerializer {
        return buf.writeInt(int).let { this }
    }

    override fun writeLong(long: Long): DataSerializer {
        return buf.writeLong(long).let { this }
    }

    override fun writeFloat(float: Float): DataSerializer {
        return buf.writeFloat(float).let { this }
    }

    override fun writeDouble(double: Double): DataSerializer {
        return buf.writeDouble(double).let { this }
    }

    override fun writeBoolean(boolean: Boolean): DataSerializer {
        return buf.writeBoolean(boolean).let { this }
    }

    override fun writeComponent(json: String): DataSerializer {
        ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buf, CraftChatMessage.fromJSON(json))
        return this
    }

    override fun build(): Any {
        return buf
    }

    override fun dataOutput(): DataOutput {
        return ByteBufOutputStream(buf)
    }

    override fun newSerializer(): DataSerializer {
        return DataSerializerFactory12005()
    }
}