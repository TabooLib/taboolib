package taboolib.module.kether

import taboolib.common.OpenContainer
import taboolib.common.platform.function.pluginId
import taboolib.common.reflect.Reflex.Companion.getProperty
import taboolib.common.reflect.Reflex.Companion.invokeMethod
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
        source.invokeMethod<Void>("setExitStatus", status)
    }

    override fun getExitStatus(): Optional<ExitStatus> {
        val status = source.invokeMethod<Optional<Any>>("getExitStatus")!!.orNull() ?: return Optional.empty()
        return Optional.of(ExitStatus(status.getProperty("running")!!, status.getProperty("waiting")!!, status.getProperty("startTime")!!))
    }

    override fun runActions(): CompletableFuture<Any> {
        return source.invokeMethod("runActions")!!
    }

    override fun getExecutor(): QuestExecutor? {
        return source.invokeMethod("getExecutor")!!
    }

    override fun terminate() {
        source.invokeMethod<Void>("terminate")
    }

    override fun rootFrame(): QuestContext.Frame {
        return RemoteFrame(remote, source.invokeMethod("rootFrame")!!)
    }

    class RemoteFrame(val remote: OpenContainer, val source: Any) : QuestContext.Frame {

        val remoteQuestContext by lazy { RemoteQuestContext(remote, source.invokeMethod("context")!!) }

        override fun close() {
            source.invokeMethod<Void>("close")
        }

        override fun name(): String {
            return source.invokeMethod("name")!!
        }

        override fun context(): QuestContext {
            return remoteQuestContext
        }

        override fun currentAction(): Optional<ParsedAction<*>> {
            val currentAction = source.invokeMethod<Optional<Any>>("currentAction")!!
            return if (currentAction.isPresent) {
                val action = currentAction.get().getProperty<Any>("action")!!
                val properties = currentAction.get().getProperty<Map<String, Any>>("properties")!!
                Optional.of(ParsedAction(RemoteQuestAction<Any>(remote, action), properties))
            } else {
                Optional.empty()
            }
        }

        override fun children(): MutableList<QuestContext.Frame> {
            return source.invokeMethod<List<Any>>("children")!!.map { RemoteFrame(remote, it) }.toMutableList()
        }

        override fun parent(): Optional<QuestContext.Frame> {
            val parent = source.invokeMethod<Optional<Any>>("parent")!!
            return if (parent.isPresent) {
                Optional.of(RemoteFrame(remote, parent.get()))
            } else {
                Optional.empty()
            }
        }

        override fun setNext(action: ParsedAction<*>) {
            val remoteAction = remote.call(StandardChannel.REMOTE_CREATE_PARSED_ACTION, arrayOf(pluginId, action.action, action.properties))
            source.invokeMethod<Void>("setNext", remoteAction.value)
        }

        override fun setNext(block: Quest.Block) {
            context().quest.getBlock(block.label).ifPresent { source.invokeMethod<Void>("setNext", it) }
        }

        override fun newFrame(name: String): QuestContext.Frame {
            return RemoteFrame(remote, source.invokeMethod<Any>("newFrame", name)!!)
        }

        override fun newFrame(action: ParsedAction<*>): QuestContext.Frame {
            val remoteAction = remote.call(StandardChannel.REMOTE_CREATE_PARSED_ACTION, arrayOf(pluginId, action.action, action.properties))
            return RemoteFrame(remote, source.invokeMethod("newFrame", remoteAction.value)!!)
        }

        override fun variables(): QuestContext.VarTable {
            return RemoteVarTable(remote, source.invokeMethod<Any>("variables")!!)
        }

        override fun <T : AutoCloseable?> addClosable(closeable: T): T {
            return source.invokeMethod("addClosable", closeable)!!
        }

        override fun <T : Any?> run(): CompletableFuture<T> {
            return source.invokeMethod("run")!!
        }

        override fun isDone(): Boolean {
            return source.invokeMethod("isDone")!!
        }
    }

    class RemoteVarTable(val remote: OpenContainer, val source: Any) : QuestContext.VarTable {

        override fun <T> get(name: String): Optional<T> {
            return source.invokeMethod("get", name)!!
        }

        override fun <T> getFuture(name: String): Optional<QuestFuture<T>> {
            return source.invokeMethod("getFuture", name)!!
        }

        override fun set(name: String, value: Any?) {
            source.invokeMethod<Void>("set", name, value)
        }

        override fun <T> set(name: String, owner: ParsedAction<T>, future: CompletableFuture<T>) {
            val remoteAction = remote.call(StandardChannel.REMOTE_CREATE_PARSED_ACTION, arrayOf(pluginId, owner.action, owner.properties))
            source.invokeMethod<Void>("set", name, remoteAction.value, future)
        }

        override fun remove(name: String) {
            source.invokeMethod<Void>("remote", name)
        }

        override fun clear() {
            source.invokeMethod<Void>("clear")
        }

        override fun keys(): MutableSet<String> {
            return source.invokeMethod("keys")!!
        }

        override fun values(): MutableCollection<MutableMap.MutableEntry<String, Any>> {
            return source.invokeMethod("values")!!
        }

        override fun initialize(frame: QuestContext.Frame) {
            source.invokeMethod<Void>("initialize", remote.call(StandardChannel.REMOTE_CREATE_FLAME, arrayOf(pluginId, frame)).value)
        }

        override fun close() {
            source.invokeMethod<Void>("close")
        }

        override fun parent(): QuestContext.VarTable {
            return RemoteVarTable(remote, source.invokeMethod("parent")!!)
        }
    }
}