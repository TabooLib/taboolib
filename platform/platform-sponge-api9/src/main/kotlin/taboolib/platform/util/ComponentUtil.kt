@file:Isolated

package taboolib.platform.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import taboolib.common.Isolated

fun String.toComponentWithFormattingCode(): TextComponent {
    return LegacyComponentSerializer.legacyAmpersand().deserialize(this)
}

fun Component.toStringWithFormattingCode(): String {
    return LegacyComponentSerializer.legacyAmpersand().serialize(this)
}

fun Component.toPlain(): String {
    return PlainTextComponentSerializer.plainText().serialize(this)
}