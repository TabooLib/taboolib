package taboolib.module.nms

import io.netty.buffer.ByteBufOutputStream
import io.netty.buffer.Unpooled
import io.netty.handler.codec.EncoderException
import net.minecraft.SystemUtils
import net.minecraft.nbt.DynamicOpsNBT
import net.minecraft.nbt.NBTCompressedStreamTools
import net.minecraft.network.PacketDataSerializer
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.network.chat.IChatBaseComponent
import java.io.DataOutput

/**
 * Adyeshach
 * taboolib.module.nms.DataSerializerFactoryImpl
 *
 * @author 坏黑
 * @since 2022/12/12 23:30
 */
class DataSerializerFactoryLegacy : DataSerializerFactory, DataSerializer {

    val buf = PacketDataSerializer(Unpooled.buffer())

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
        // 1.20.2 没有 ComponentSerialization, 23w40a (1.20.3的快照) 之后加的
        if (MinecraftVersion.majorLegacy >= 12003) {
            val component = IChatBaseComponent.ChatSerializer.fromJson(json)
            val nbt = SystemUtils.getOrThrow(ComponentSerialization.CODEC.encodeStart(DynamicOpsNBT.INSTANCE, component)) { EncoderException("Failed to encode: $it $component") }
            NBTCompressedStreamTools.writeAnyTag(nbt, ByteBufOutputStream(buf))
        } else {
            writeUtf(json, 262144)
        }
        return this
    }

    override fun build(): Any {
        return buf
    }

    override fun dataOutput(): DataOutput {
        return ByteBufOutputStream(buf)
    }

    override fun newSerializer(): DataSerializer {
        return DataSerializerFactoryLegacy()
    }
}