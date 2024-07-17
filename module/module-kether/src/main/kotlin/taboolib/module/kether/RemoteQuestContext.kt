package taboolib.module.kether

import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import taboolib.common.OpenContainer
import taboolib.common.platform.function.pluginId
import taboolib.common.util.orNull
import taboolib.library.kether.*
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * TabooLib
 * taboolib.module.kether.RemoteQuestContext
 *
 * @author sky
 * @since 2021/8/11 12:04 上午
 */
class RemoteQuestContext(val remote: OpenContainer, val source: Any) : ScriptContext(ScriptService, RemoteQuest(remote, source.invokeMethod("getQuest")!!)) {

    override fun getService(): QuestService<ScriptContext> {
        error("remote context")
    }

    override fun setExitStatus(exitStatus: ExitStatus) {
        val status = remote.call(StandardChannel.REMOTE_CREATE_EXIT_STATUS, arrayOf(exitStatus.isRunning, exitStatus.isWaiting, exitStatus.startTime))
        source.invokeMethod<Void>("setExitStatus", status, remap = false)
    }

    override fun getExitStatus(): Optional<ExitStatus> {
        val status = source.invokeMethod<Optional<Any>>("getExitStatus", remap = false)!!.orNull() ?: return Optional.empty()
        return Optional.of(ExitStatus(status.getProperty("running")!!, status.getProperty("waiting")!!, status.getProperty("startTime")!!))
    }

    override fun runActions(): CompletableFuture<Any> {
        return source.invokeMethod("runActions", remap = false)!!
    }

    override fun getExecutor(): QuestExecutor? {
        return source.invokeMethod("getExecutor", remap = false)!!
    }

    override fun terminate() {
        source.invokeMethod<Void>("terminate", remap = false)
    }

    override fun rootFrame(): QuestContext.Frame {
        return RemoteFrame(remote, source.invokeMethod("rootFrame", remap = false)!!)
    }

    class RemoteFrame(val remote: OpenContainer, val source: Any) : QuestContext.Frame {

        val remoteQuestContext by lazy { RemoteQuestContext(remote, source.invokeMethod("context", remap = false)!!) }

        override fun close() {
            source.invokeMethod<Void>("close", remap = false)
        }

        override fun name(): String {
            return source.invokeMethod("name", remap = false)!!
        }

        override fun context(): QuestContext {
            return remoteQuestContext
        }

        override fun currentAction(): Optional<ParsedAction<*>> {
            val currentAction = source.invokeMethod<Optional<Any>>("currentAction", remap = false)!!
            return if (currentAction.isPresent) {
                val action = currentAction.get().getProperty<Any>("action")!!
                val properties = currentAction.get().getProperty<Map<String, Any>>("properties")!!
                Optional.of(ParsedAction(RemoteQuestAction<Any>(remote, action), properties))
            } else {
                Optional.empty()
            }
        }

        override fun children(): MutableList<QuestContext.Frame> {
            return source.invokeMethod<List<Any>>("children", remap = false)!!.map { RemoteFrame(remote, it) }.toMutableList()
        }

        override fun parent(): Optional<QuestContext.Frame> {
            val parent = source.invokeMethod<Optional<Any>>("parent", remap = false)!!
            return if (parent.isPresent) {
                Optional.of(RemoteFrame(remote, parent.get()))
            } else {
                Optional.empty()
            }
        }

        override fun setNext(action: ParsedAction<*>) {
            val remoteAction = remote.call(StandardChannel.REMOTE_CREATE_PARSED_ACTION, arrayOf(pluginId, action.action, action.properties))
            source.invokeMethod<Void>("setNext", remoteAction.value, remap = false)
        }

        override fun setNext(block: Quest.Block) {
            context().quest.getBlock(block.label).ifPresent { source.invokeMethod<Void>("setNext", it, remap = false) }
        }

        override fun newFrame(name: String): QuestContext.Frame {
            return RemoteFrame(remote, source.invokeMethod<Any>("newFrame", name, remap = false)!!)
        }

        override fun newFrame(action: ParsedAction<*>): QuestContext.Frame {
            val remoteAction = remote.call(StandardChannel.REMOTE_CREATE_PARSED_ACTION, arrayOf(pluginId, action.action, action.properties))
            return RemoteFrame(remote, source.invokeMethod("newFrame", remoteAction.value, remap = false)!!)
        }

        override fun variables(): QuestContext.VarTable {
            return RemoteVarTable(remote, source.invokeMethod<Any>("variables", remap = false)!!)
        }

        override fun <T : AutoCloseable?> addClosable(closeable: T): T {
            return source.invokeMethod("addClosable", closeable, remap = false)!!
        }

        override fun <T : Any?> run(): CompletableFuture<T> {
            return source.invokeMethod("run", remap = false)!!
        }

        override fun isDone(): Boolean {
            return source.invokeMethod("isDone", remap = false)!!
        }
    }

    class RemoteVarTable(val remote: OpenContainer, val source: Any) : QuestContext.VarTable {

        override fun <T> get(name: String): Optional<T> {
            return source.invokeMethod("get", name, remap = false)!!
        }

        override fun <T> getFuture(name: String): Optional<QuestFuture<T>> {
            return source.invokeMethod("getFuture", name, remap = false)!!
        }

        override fun set(name: String, value: Any?) {
            source.invokeMethod<Void>("set", name, value, remap = false)
        }

        override fun <T> set(name: String, owner: ParsedAction<T>, future: CompletableFuture<T>) {
            val remoteAction = remote.call(StandardChannel.REMOTE_CREATE_PARSED_ACTION, arrayOf(pluginId, owner.action, owner.properties))
            source.invokeMethod<Void>("set", name, remoteAction.value, future, remap = false)
        }

        override fun remove(name: String) {
            source.invokeMethod<Void>("remote", name, remap = false)
        }

        override fun clear() {
            source.invokeMethod<Void>("clear", remap = false)
        }

        override fun keys(): MutableSet<String> {
            return source.invokeMethod("keys", remap = false)!!
        }

        override fun values(): MutableCollection<MutableMap.MutableEntry<String, Any>> {
            return source.invokeMethod("values", remap = false)!!
        }

        override fun initialize(frame: QuestContext.Frame) {
            source.invokeMethod<Void>("initialize", remote.call(StandardChannel.REMOTE_CREATE_FLAME, arrayOf(pluginId, frame)).value, remap = false)
        }

        override fun close() {
            source.invokeMethod<Void>("close", remap = false)
        }

        override fun parent(): QuestContext.VarTable {
            return RemoteVarTable(remote, source.invokeMethod("parent", remap = false)!!)
        }
    }
}