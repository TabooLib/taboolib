package taboolib.module.kether

import com.google.common.collect.ImmutableMap
import com.google.common.collect.Multimap
import io.izzel.kether.common.api.DefaultRegistry
import io.izzel.kether.common.api.Quest
import io.izzel.kether.common.api.QuestRegistry
import io.izzel.kether.common.api.QuestService
import taboolib.common.platform.getJarFile
import taboolib.common5.reflect.Reflex.Companion.staticInvoke
import taboolib.common5.util.replaceWithOrder
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
class ScriptService : QuestService<ScriptContext> {

    companion object {

        @JvmField
        val INSTANCE = QuestService::class.java.staticInvoke<ScriptService>("instance")!!
    }

    private val registry = DefaultRegistry()
    private val syncExecutor = ScriptSchedulerExecutor
    private val asyncExecutor = Executors.newScheduledThreadPool(2)
    private val locale = SecuredFile.loadConfiguration(ScriptService::class.java.getResourceAsStream("kether.yml").readBytes().toString(StandardCharsets.UTF_8))

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