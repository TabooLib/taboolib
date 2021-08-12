package taboolib.module.kether

import taboolib.library.kether.QuestAction
import taboolib.library.kether.QuestActionParser
import taboolib.library.kether.QuestReader
import java.util.function.Function

/**
 * TabooLib
 * taboolib.module.kether.ScriptActionParser
 *
 * @author sky
 * @since 2021/8/9 12:29 上午
 */
class ScriptActionParser<T>(val reader: QuestReader.() -> QuestAction<*>) : QuestActionParser {

    @Suppress("UNCHECKED_CAST")
    override fun <T> resolve(resolver: QuestReader): QuestAction<T> {
        return reader(resolver) as QuestAction<T>
    }
}