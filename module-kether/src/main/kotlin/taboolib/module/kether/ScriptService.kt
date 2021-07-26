package taboolib.module.kether

import com.google.common.collect.ImmutableMap
import com.google.common.collect.Multimap
import io.izzel.kether.common.api.DefaultRegistry
import io.izzel.kether.common.api.QuestRegistry
import io.izzel.kether.common.api.QuestService
import io.izzel.kether.common.api.ServiceHolder
import taboolib.common.platform.getDataFolder
import taboolib.common.reflect.Reflex.Companion.setProperty
import taboolib.common.util.replaceWithOrder
import taboolib.module.configuration.Config
import taboolib.module.configuration.SecuredFile
import java.io.File
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
            ServiceHolder::class.java.setProperty("INSTANCE", ScriptService, fixed = true)
        } catch (ex: Throwable) {
            ex.printStackTrace()
        }
    }

    private val registry = DefaultRegistry()
    private val syncExecutor = ScriptSchedulerExecutor
    private val asyncExecutor = Executors.newScheduledThreadPool(2)

    @Config("kether.yml")
    private lateinit var locale: SecuredFile

    val mainspace by lazy {
        Workspace(File(getDataFolder(), "kether"))
    }

    override fun getRegistry(): QuestRegistry {
        return registry
    }

    override fun getQuest(id: String): Optional<Script> {
        return Optional.ofNullable(mainspace.scripts[id])
    }

    override fun getQuestSettings(id: String): Map<String, Any?> {
        return Collections.unmodifiableMap(mainspace.scriptsSetting.getOrDefault(id, ImmutableMap.of()))
    }

    override fun getQuests(): Map<String, Script> {
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