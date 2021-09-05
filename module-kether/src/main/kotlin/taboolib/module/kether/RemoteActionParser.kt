package taboolib.module.kether

import taboolib.common.OpenContainer
import taboolib.common.platform.function.pluginId
import taboolib.library.kether.QuestAction
import taboolib.library.kether.QuestActionParser
import taboolib.library.kether.QuestReader

/**
 * TabooLib
 * taboolib.module.kether.RemoteActionParser
 *
 * @author sky
 * @since 2021/8/10 3:49 下午
 */
class RemoteActionParser(val remote: OpenContainer, val action: String, val namespace: String = "kether") : QuestActionParser {

    override fun <T> resolve(resolver: QuestReader): QuestAction<T> {
        // 远程读取动作
        val result = remote.call(StandardChannel.REMOTE_RESOLVE, arrayOf(pluginId, resolver, action, namespace))
        if (result.isFailed) {
            error("Unable to create remote action $namespace:$action")
        }
        return RemoteQuestAction(remote, result.value!!)
    }
}