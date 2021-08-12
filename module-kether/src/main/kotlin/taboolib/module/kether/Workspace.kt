package taboolib.module.kether

import com.google.common.collect.ImmutableList
import com.google.common.collect.MultimapBuilder
import taboolib.common.platform.getProxyPlayer
import taboolib.common.platform.warning
import taboolib.common5.Coerce
import taboolib.library.kether.ExitStatus
import taboolib.library.kether.Quest
import taboolib.module.kether.action.ActionProperty
import java.io.File

/**
 * TabooLibKotlin
 * taboolib.module.ketherx.Workspace
 *
 * @author sky
 * @since 2021/1/26 3:26 下午
 */
@Suppress("UnstableApiUsage")
class Workspace(val file: File, val extension: String = ".ks", val namespace: List<String> = emptyList()) {

    val scripts = HashMap<String, Script>()
    val scriptsSetting = HashMap<String, Map<String, Any?>>()
    val runningScripts = MultimapBuilder.hashKeys().arrayListValues().build<String, ScriptContext>()!!

//    val listeners = ArrayList<AutoCloseable>()

    fun loadAll() {
//        listeners.forEach { it.close() }
//        listeners.clear()

        loadScripts()
        loadSettings()

        scripts.forEach {
            if (Coerce.toBoolean(scriptsSetting[it.value.id]?.get("autostart"))) {
                ScriptService.startQuest(ScriptContext.create(it.value))
                return@forEach
            }
            val trigger = scriptsSetting[it.value.id]?.get("start") ?: return@forEach
            val operator = Kether.getEvent(trigger.toString())
            if (operator == null) {
                warning("Unknown starting trigger $trigger")
                return@forEach
            }
//            listeners.add(Closables.listening<Any>(operator) { e ->
//                val context = ScriptContext.create(it.value) {
//                    val player = ActionProperty.getScriptProperty(e, "bind")
//                    if (player != null) {
//                        sender = getProxyPlayer(player.toString())
//                        id = "${it.value.id}:${sender?.name}"
//                    } else {
//                        id = "${it.value.id}:$trigger"
//                    }
//                    event = e
//                }
//                runScript(context.id, context)
//            })
        }
    }

    fun loadSettings() {
        scriptsSetting.clear()
        scripts.values.forEach { quest ->
            val context = ScriptContext.create(quest)
            quest.getBlock("settings").ifPresent {
                it.actions.forEach { action ->
                    action.process(context.rootFrame())
                }
            }
            scriptsSetting[quest.id] = context.rootFrame().deepVars()
        }
    }

    fun loadScripts() {
        if (!file.exists()) {
            file.mkdirs()
        }
        scripts.clear()
        val loader = KetherScriptLoader()
        val folder = file.toPath()
        val scriptMap = HashMap<String, Quest>()
        if (java.nio.file.Files.notExists(folder)) {
            java.nio.file.Files.createDirectories(folder)
        }
        val iterator = java.nio.file.Files.walk(folder).iterator()
        while (iterator.hasNext()) {
            val path = iterator.next()
            if (!java.nio.file.Files.isDirectory(path)) {
                try {
                    val name = folder.relativize(path).toString().replace(File.separatorChar, '.')
                    if (name.endsWith(extension)) {
                        val bytes = path.toFile().readBytes()
                        scriptMap[name] = loader.load(ScriptService, name, bytes, namespace)
                    }
                } catch (e: Exception) {
                    warning("Unexpected exception while parsing kether script:")
                    e.localizedMessage?.split('\n')?.forEach { warning(it) }
                }
            }
        }
        scripts.putAll(scriptMap)
    }

    fun cancelAll() {
        getRunningScript().forEach { terminateScript(it) }
    }

    fun getRunningScript(): List<ScriptContext> {
        return ImmutableList.copyOf(runningScripts.values())
    }

    fun runScript(id: String, context: ScriptContext) {
        context.id = id
        runningScripts.put(id, context)
        context.runActions().thenRunAsync({
            runningScripts.remove(id, context)
        }, ScriptService.executor)
    }

    fun terminateScript(context: ScriptContext) {
        if (!context.exitStatus.isPresent) {
            context.setExitStatus(ExitStatus.paused())
            runningScripts.remove(context.id, context)
        }
    }
}