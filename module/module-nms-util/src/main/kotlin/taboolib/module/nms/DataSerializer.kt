package taboolib.module.nms

import com.mojang.authlib.properties.PropertyMap
import io.netty.handler.codec.EncoderException
import java.util.*

/**
 * Adyeshach
 * taboolib.module.nms.DataSerializer
 *
 * @author 坏黑
 * @since 2022/12/12 23:00
 */
interface DataSerializer {

    fun writeByte(byte: Byte): DataSerializer

    fun writeBytes(bytes: ByteArray): DataSerializer

    fun writeShort(short: Short): DataSerializer

    fun writeInt(int: Int): DataSerializer

    fun writeLong(long: Long): DataSerializer

    fun writeFloat(float: Float): DataSerializer

    fun writeDouble(double: Double): DataSerializer

    fun writeBoolean(boolean: Boolean): DataSerializer

    fun writeMetadata(meta: List<Any>): DataSerializer

    fun writeUUID(uuid: UUID): DataSerializer {
        writeLong(uuid.mostSignificantBits)
        writeLong(uuid.leastSignificantBits)
        return this
    }

    fun writeBlockPosition(x: Int, y: Int, z: Int): DataSerializer {
        writeLong((x.toLong() and 67108863 shl 38) or (y.toLong() and 4095 shl 26) or (z.toLong() and 67108863 shl 0))
        return this
    }

    fun writeVarIntArray(intArray: IntArray): DataSerializer {
        writeVarInt(intArray.size)
        intArray.forEach { writeVarInt(it) }
        return this
    }

    fun writeVarInt(int: Int): DataSerializer {
        var i = int
        while (i and -128 != 0) {
            writeByte((i and 127 or 128).toByte())
            i = i ushr 7
        }
        writeByte(i.toByte())
        return this
    }

    fun <T> writeNullable(value: T?, writer: (T) -> Unit): DataSerializer {
        if (value == null) {
            writeBoolean(false)
        } else {
            writeBoolean(true)
            writer(value)
        }
        return this
    }

    fun <E : Enum<E>> writeEnumSet(enumSet: EnumSet<E>, enumClass: Class<E>): DataSerializer {
        val enumConstants = enumClass.enumConstants
        val bitSet = BitSet(enumConstants.size)
        for (i in enumConstants.indices) {
            bitSet.set(i, enumSet.contains(enumConstants[i]))
        }
        writeFixedBitSet(bitSet, enumConstants.size)
        return this
    }

    fun writeFixedBitSet(bitSet: BitSet, size: Int): DataSerializer {
        if (bitSet.length() > size) {
            throw EncoderException("BitSet is larger than expected size (${bitSet.length()}>$size)")
        } else {
            writeBytes(bitSet.toByteArray().copyOf(-Math.floorDiv(-size, 8)))
        }
        return this
    }

    fun writeString(string: String): DataSerializer {
        val arr = string.toByteArray(Charsets.UTF_8)
        if (arr.size > 32767) {
            throw EncoderException("String too big (was ${string.length} bytes encoded, max 32767)")
        } else {
            writeVarInt(arr.size)
            writeBytes(arr)
        }
        return this
    }

    fun writeUtf(string: String, length: Int = 32767): DataSerializer {
        if (string.length > length) {
            throw EncoderException("String too big (was ${string.length} bytes encoded, max 32767)")
        } else {
            val arr = string.encodeToByteArray()
            val maxEncodedUtfLength = length * 3
            if (arr.size > maxEncodedUtfLength) {
                throw EncoderException("String too big (was ${arr.size} bytes encoded, max $maxEncodedUtfLength)")
            } else {
                writeVarInt(arr.size)
                writeBytes(arr)
            }
        }
        return this
    }

    fun writeGameProfileProperties(map: PropertyMap): DataSerializer {
        writeVarInt(map.size())
        map.values().forEach { property ->
            writeUtf(property.name)
            writeUtf(property.value)
            if (property.hasSignature()) {
                writeBoolean(true)
                writeUtf(property.signature)
            } else {
                writeBoolean(false)
            }
        }
        return this
    }

    fun build(): Any
}