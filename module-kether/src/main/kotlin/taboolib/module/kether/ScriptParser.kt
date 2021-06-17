package taboolib.module.kether

import io.izzel.kether.common.api.KetherCompleters
import io.izzel.kether.common.api.QuestAction
import io.izzel.kether.common.api.QuestActionParser
import io.izzel.kether.common.loader.QuestReader

/**
 * Adyeshach
 * ink.ptms.adyeshach.common.script.ScriptAction
 *
 * @author sky
 * @since 2021/1/20 11:45 上午
 */
object ScriptParser {

    fun <T> parser(resolve: (QuestReader) -> QuestAction<T>): QuestActionParser {
        return object : QuestActionParser {

            @Suppress("UNCHECKED_CAST")
            override fun <T> resolve(resolver: QuestReader): QuestAction<T> {
                return resolve.invoke(resolver) as QuestAction<T>
            }

            override fun complete(params: MutableList<String>): MutableList<String> {
                return KetherCompleters.seq(KetherCompleters.consume()).apply(params)
            }
        }
    }
}