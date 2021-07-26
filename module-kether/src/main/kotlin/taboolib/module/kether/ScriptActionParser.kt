package taboolib.module.kether

import io.izzel.kether.common.api.KetherCompleters
import io.izzel.kether.common.api.QuestAction
import io.izzel.kether.common.api.QuestActionParser
import io.izzel.kether.common.loader.QuestReader
import java.io.Serializable

/**
 * TabooLib
 * taboolib.module.kether.ScriptActionParser
 *
 * @author sky
 * @since 2021/7/26 4:32 下午
 */
class ScriptActionParser<T>(val resolve: (QuestReader) -> QuestAction<T>) : QuestActionParser, Serializable {

    companion object {

        private const val serialVersionUID = 1L
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> resolve(resolver: QuestReader): QuestAction<T> {
        return resolve.invoke(resolver) as QuestAction<T>
    }

    override fun complete(params: MutableList<String>): MutableList<String> {
        return KetherCompleters.seq(KetherCompleters.consume()).apply(params)
    }
}