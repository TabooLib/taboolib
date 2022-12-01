package taboolib.module.kether

import com.mojang.datafixers.kinds.App
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.warning
import taboolib.library.kether.*
import taboolib.library.kether.Parser.*
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.CompletableFuture

typealias Script = Quest

typealias ScriptFrame = QuestContext.Frame

/**
 * 运行 Kether 语句并打印错误
 */
fun <T> runKether(func: () -> T): T? {
    try {
        return func()
    } catch (ex: Exception) {
        ex.printKetherErrorMessage()
    }
    return null
}

/**
 * 创建 ScriptParser 对象
 */
fun <T> scriptParser(resolve: (QuestReader) -> QuestAction<T>): ScriptActionParser<T> {
    return ScriptActionParser(resolve)
}

fun <T> combinationParser(builder: ParserHolder.(Instance) -> App<Mu, Action<T>>): ScriptActionParser<T> {
    val parser = build(builder(ParserHolder, Instance()))
    return ScriptActionParser { parser.resolve<T>(this) }
}

/**
 * 从字符串创建 Script 对象
 */
fun String.parseKetherScript(namespace: List<String> = emptyList()): Script {
    return KetherScriptLoader().load(ScriptService, "temp_${UUID.randomUUID()}", toByteArray(StandardCharsets.UTF_8), namespace)
}

/**
 * 从字符串列表创建 Script 对象
 */
fun List<String>.parseKetherScript(namespace: List<String> = emptyList()): Script {
    return joinToString("\n").parseKetherScript(namespace)
}

/**
 * 在 Frame 中运行 ParsedAction
 */
fun ScriptFrame.run(action: ParsedAction<*>): CompletableFuture<Any?> {
    return newFrame(action).run()
}

/**
 * 获取玩家
 */
fun ScriptFrame.player(): ProxyPlayer {
    return script().sender as? ProxyPlayer ?: error("No player selected.")
}

/**
 * 获取脚本上下文
 */
fun ScriptFrame.script(): ScriptContext {
    return context() as ScriptContext
}

/**
 * 继承变量
 */
fun ScriptContext.extend(map: Map<String, Any?>) {
    rootFrame().variables().run { map.forEach { (k, v) -> set(k, v) } }
}

/**
 * 获取所有变量
 */
fun ScriptFrame.deepVars(): HashMap<String, Any?> {
    val map = HashMap<String, Any?>()
    var parent = parent()
    while (parent.isPresent) {
        map.putAll(parent.get().variables().toMap())
        parent = parent.get().parent()
    }
    map.putAll(variables().toMap())
    return map
}

/**
 * 打印 Kether 错误信息
 */
fun Throwable.printKetherErrorMessage() {
    if (javaClass.name.endsWith("kether.LocalizedException")) {
        warning("Unexpected exception while parsing kether script:")
        localizedMessage.split('\n').forEach { warning(it) }
    } else {
        printStackTrace()
    }
}

/**
 * 类型适配
 */
fun Any?.inferType(): Any? {
    if (this !is String) return this
    toIntOrNull()?.let { return it }
    toLongOrNull()?.let { return it }
    toDoubleOrNull()?.let { return it }
    toBooleanStrictOrNull()?.let { return it }
    return this
}