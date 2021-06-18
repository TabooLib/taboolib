package taboolib.module.lang

import taboolib.library.configuration.FileConfiguration
import taboolib.module.configuration.SecuredFile
import java.io.File
import java.io.InputStream
import java.nio.charset.StandardCharsets

/**
 * TabooLib
 * taboolib.module.lang.LangFile
 *
 * @author sky
 * @since 2021/6/18 11:04 下午
 */
class LanguageFile(val source: FileConfiguration) {

    init {

    }

    companion object {

        fun create(file: File): LanguageFile {
            return LanguageFile(SecuredFile.loadConfiguration(file))
        }

        fun create(inputStream: InputStream): LanguageFile {
            return LanguageFile(SecuredFile.loadConfiguration(inputStream.readBytes().toString(StandardCharsets.UTF_8)))
        }
    }
}