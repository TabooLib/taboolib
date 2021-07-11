package taboolib.platform.util

import org.spongepowered.api.text.Text
import org.spongepowered.api.text.serializer.TextSerializers

object TextUtil {
    /**
     * @param str The string with formatting code, for example: "&4Warning!"
     * @return Text object
     */
    fun toTextWithFormattingCode(str: String?): Text {
        return TextSerializers.FORMATTING_CODE.deserialize(str)
    }

    /**
     * @param text Text object
     * @return The string with formatting code, for example: "&4Warning!"
     */
    fun toPlainWithFormattingCode(text: Text?): String {
        return TextSerializers.FORMATTING_CODE.serialize(text)
    }

    /**
     * @param text Text object
     * @return The string without formatting code
     */
    fun toPlain(text: Text?): String {
        return TextSerializers.PLAIN.serialize(text)
    }
}