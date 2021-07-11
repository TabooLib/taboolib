@file:Isolated

package taboolib.platform.util

import org.spongepowered.api.text.Text
import org.spongepowered.api.text.serializer.TextSerializers
import taboolib.common.Isolated

/**
 * The string with formatting code, for example: "&4Warning!"
 * @return Text object
 */
fun String.toTextWithFormattingCode(): Text {
    return TextSerializers.FORMATTING_CODE.deserialize(this)
}

/**
 * Text object
 * @return The string with formatting code, for example: "&4Warning!"
 */
fun Text.toPlainWithFormattingCode(): String {
    return TextSerializers.FORMATTING_CODE.serialize(this)
}