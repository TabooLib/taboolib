package taboolib.common.platform

import org.tabooproject.reflex.serializer.BinarySerializable
import org.tabooproject.reflex.serializer.BinaryWriter

class AwakeClass(val name: String, val isClassVisitor: Boolean, val platformService: List<String>) : BinarySerializable {

    override fun writeTo(writer: BinaryWriter) {
        writer.writeNullableString(name)
        writer.writeBoolean(isClassVisitor)
        writer.writeList(platformService) { writer.writeNullableString(it) }
    }
}