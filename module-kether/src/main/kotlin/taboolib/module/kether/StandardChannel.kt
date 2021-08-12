package taboolib.module.kether

import taboolib.common.OpenListener
import taboolib.common.OpenResult
import taboolib.common.platform.Awake
import taboolib.common.platform.getOpenContainer
import taboolib.common.platform.info
import taboolib.library.kether.ExitStatus
import taboolib.library.kether.ParsedAction

@Awake
object StandardChannel : OpenListener {

    const val REMOTE_RESOLVE = "kether_remote_resolve"

    const val REMOTE_CREATE_FLAME = "kether_create_frame"

    const val REMOTE_CREATE_EXIT_STATUS = "kether_create_exit_status"

    const val REMOTE_CREATE_PARSED_ACTION = "kether_create_parsed_action"

    const val REMOTE_SHARED_ACTION = "kether_shared_action"

    @Suppress("UNCHECKED_CAST")
    override fun call(name: String, data: Array<Any>): OpenResult {
        return when (name) {
            REMOTE_RESOLVE -> {
                val reader = KetherScriptLoader.RemoteReader(getOpenContainer(data[0].toString())!!, data[1])
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
                return OpenResult.successful(ParsedAction(action, data[2] as MutableMap<String, Any>))
            }
            REMOTE_SHARED_ACTION -> {
                val remote = getOpenContainer(data[0].toString())!!
                (data[1] as Array<String>).forEach {
                    Kether.addAction(it, RemoteActionParser(remote, it, data[2].toString()), data[2].toString())
                }
                return OpenResult.successful()
            }
            else -> OpenResult.failed()
        }
    }
}