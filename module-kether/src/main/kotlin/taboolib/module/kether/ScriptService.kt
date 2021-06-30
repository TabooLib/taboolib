package taboolib.module.kether

import com.google.common.collect.ImmutableMap
import com.google.common.collect.Multimap
import io.izzel.kether.common.api.*
import taboolib.common.platform.getJarFile
import taboolib.common.reflect.Reflex.Companion.reflex
import taboolib.common.reflect.Reflex.Companion.static
import taboolib.common.reflect.Reflex.Companion.staticInvoke
import taboolib.common.util.replaceWithOrder
import taboolib.module.configuration.Config
import taboolib.module.configuration.SecuredFile
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

/**
 * @author IzzelAliz
 */
object ScriptService : QuestService<ScriptContext> {

    init {
        try {
            ServiceHolder::class.java.static("INSTANCE", ScriptService)
        } catch (ex: Throwable) {
            ex.printStackTrace()
        }
    }

    private val registry = DefaultRegistry()
    private val syncExecutor = ScriptSchedulerExecutor
    private val asyncExecutor = Executors.newScheduledThreadPool(2)

    @Config("kether.yml")
    private lateinit var locale: SecuredFile

    val mainspace = Workspace(File("kether/${getJarFile().nameWithoutExtension}"))

    override fun getRegistry(): QuestRegistry {
        return registry
    }

    override fun getQuest(id: String): Optional<Quest> {
        return Optional.ofNullable(mainspace.scripts[id])
    }

    override fun getQuestSettings(id: String): Map<String, Any?> {
        return Collections.unmodifiableMap(mainspace.scriptsSetting.getOrDefault(id, ImmutableMap.of()))!!
    }

    override fun getQuests(): Map<String, Quest> {
        return Collections.unmodifiableMap(mainspace.scripts)
    }

    override fun getRunningQuests(): Multimap<String, ScriptContext> {
        return mainspace.runningScripts
    }

    override fun getRunningQuests(playerIdentifier: String): List<ScriptContext> {
        return Collections.unmodifiableList(mainspace.runningScripts[playerIdentifier])
    }

    override fun getExecutor(): Executor {
        return syncExecutor
    }

    override fun getAsyncExecutor(): ScheduledExecutorService {
        return asyncExecutor
    }

    override fun getLocalizedText(node: String, vararg params: Any): String {
        return locale.getString(node, "<ERROR:${node}>:${params.joinToString(",") { it.toString() }}").replaceWithOrder(*params)
    }

    override fun startQuest(context: ScriptContext) {
        mainspace.runScript(UUID.randomUUID().toString(), context)
    }

    override fun terminateQuest(context: ScriptContext) {
        mainspace.terminateScript(context)
    }
}