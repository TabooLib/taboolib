package taboolib.module.kether

import taboolib.common.OpenListener
import taboolib.common.OpenResult
import taboolib.common.io.groupId
import taboolib.common.platform.Awake
import taboolib.common.platform.function.getOpenContainer
import taboolib.common.reflect.Reflex.Companion.getProperty
import taboolib.library.kether.ExitStatus
import taboolib.library.kether.ParsedAction

@Awake
object StandardChannel : OpenListener {

    const val REMOTE_RESOLVE = "kether_remote_resolve"

    const val REMOTE_CREATE_FLAME = "kether_create_frame"

    const val REMOTE_CREATE_EXIT_STATUS = "kether_create_exit_status"

    const val REMOTE_CREATE_PARSED_ACTION = "kether_create_parsed_action"

    const val REMOTE_ADD_ACTION = "kether_add_action"

    const val REMOTE_REMOVE_ACTION = "kether_remove_action"

    const val REMOTE_ADD_PROPERTY = "kether_add_property"

    const val REMOTE_REMOVE_PROPERTY = "kether_remove_property"

    @Suppress("UNCHECKED_CAST")
    override fun call(name: String, data: Array<Any>): OpenResult {
        return when (name) {
            REMOTE_RESOLVE -> {
                val reader = RemoteQuestReader(getOpenContainer(data[0].toString())!!, data[1])
                val parser = Kether.scriptRegistry.getParser(data[2].toString(), data[3].toString())
                if (parser.isPresent) {
                    OpenResult.successful(parser.get().resolve<Any>(reader))
                } else {
                    OpenResult.failed()
                }
            }
            REMOTE_CREATE_FLAME -> {
                OpenResult.successful(RemoteQuestContext.RemoteFrame(getOpenContainer(data[0].toString())!!, data[1]))
            }
            REMOTE_CREATE_EXIT_STATUS -> {
                OpenResult.successful(ExitStatus(data[0] as Boolean, data[1] as Boolean, data[2] as Long))
            }
            REMOTE_CREATE_PARSED_ACTION -> {
                val remote = getOpenContainer(data[0].toString())!!
                val action = RemoteQuestAction<Any>(remote, data[1])
                OpenResult.successful(ParsedAction(action, data[2] as MutableMap<String, Any>))
            }
            REMOTE_ADD_ACTION -> {
                val remote = getOpenContainer(data[0].toString())!!
                (data[1] as Array<String>).forEach {
                    Kether.addAction(it, RemoteActionParser(remote, it, data[2].toString()), data[2].toString())
                }
                OpenResult.successful()
            }
            REMOTE_REMOVE_ACTION -> {
                (data[0] as Array<String>).forEach {
                    Kether.removeAction(it, data[1].toString())
                }
                OpenResult.successful()
            }
            REMOTE_ADD_PROPERTY -> {
                val remote = getOpenContainer(data[0].toString())!!
                var bind = data[1].toString()
                bind = if (bind.startsWith("@")) "$groupId${bind.substring(1)}" else bind
                try {
                    Kether.addScriptProperty(Class.forName(bind), RemoteScriptProperty(remote, data[2], data[2].getProperty("id")!!))
                    OpenResult.successful()
                } catch (ex: ClassNotFoundException) {
                    OpenResult.failed()
                }
            }
            REMOTE_REMOVE_PROPERTY -> {
                var bind = data[0].toString()
                bind = if (bind.startsWith("@")) "$groupId${bind.substring(1)}" else bind
                try {
                    Kether.removeScriptProperty(Class.forName(bind), data[1].toString())
                    OpenResult.successful()
                } catch (ex: ClassNotFoundException) {
                    OpenResult.failed()
                }
            }
            else -> OpenResult.failed()
        }
    }
}