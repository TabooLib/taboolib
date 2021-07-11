package taboolib.platform.util

import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

object ComponentUtil {
    fun toComponentWithFormattingCode(str: String?): TextComponent {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(str!!)
    }

    fun toStringWithFormattingCode(component: TextComponent?): String {
        return LegacyComponentSerializer.legacyAmpersand().serialize(
            component!!
        )
    }

    fun toPlain(component: TextComponent?): String {
        return PlainTextComponentSerializer.plainText().serialize(component!!)
    }
}