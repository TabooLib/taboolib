package taboolib.module.kether

import taboolib.common.OpenContainer
import taboolib.common.platform.function.pluginId
import taboolib.common.reflect.Reflex.Companion.invokeMethod
import taboolib.library.kether.QuestAction
import taboolib.library.kether.QuestContext
import java.util.concurrent.CompletableFuture

/**
 * TabooLib
 * taboolib.module.kether.RemoteQuestAction
 *
 * @author sky
 * @since 2021/8/10 3:51 下午
 */
class RemoteQuestAction<T>(val remote: OpenContainer, val source: Any) : QuestAction<T>() {

    override fun process(frame: QuestContext.Frame): CompletableFuture<T> {
        return source.invokeMethod("process", remote.call(StandardChannel.REMOTE_CREATE_FLAME, arrayOf(pluginId, frame)).value)!!
    }
}