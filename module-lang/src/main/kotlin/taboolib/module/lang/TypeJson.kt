package taboolib.module.lang

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.util.VariableReader
import taboolib.common.util.asList
import taboolib.common.util.replaceWithOrder
import taboolib.module.chat.TellrawJson

/**
 * TabooLib
 * taboolib.module.lang.TypeJson
 *
 * @author sky
 * @since 2021/6/20 10:55 下午
 */
class TypeJson : Type {

    var text: List<String>? = null
    var json = ArrayList<List<VariableReader.Part>>()
    var jsonArgs = ArrayList<Map<String, Any>>()

    override fun init(source: Map<String, Any>) {
        text = source["text"]?.asList()
        json.addAll(text?.map { VariableReader(it).parts } ?: emptyList())
        try {
            jsonArgs.addAll((source["args"] as List<*>).map { (it as Map<*, *>).map { (k, v) -> k.toString() to v!! }.toMap() })
        } catch (ex: ClassCastException) {
        }
    }

    override fun send(sender: ProxyCommandSender, vararg args: Any) {
        TellrawJson().sendTo(sender) {
            var i = 0
            json.forEachIndexed { index, line ->
                line.forEach { part ->
                    append(part.text.replaceWithOrder(*args).translate(sender))
                    if (part.isVariable) {
                        val arg = jsonArgs.getOrNull(++i)
                        if (arg != null) {
                            if (arg.containsKey("hover")) {
                                hoverText(arg["hover"].toString().replaceWithOrder(*args).translate(sender))
                            }
                            if (arg.containsKey("command")) {
                                runCommand(arg["command"].toString().replaceWithOrder(*args).translate(sender))
                            }
                            if (arg.containsKey("suggest")) {
                                suggestCommand(arg["suggest"].toString().replaceWithOrder(*args).translate(sender))
                            }
                            if (arg.containsKey("insertion")) {
                                insertion(arg["insertion"].toString().replaceWithOrder(*args).translate(sender))
                            }
                            if (arg.containsKey("copy")) {
                                copyToClipboard(arg["copy"].toString().replaceWithOrder(*args).translate(sender))
                            }
                            if (arg.containsKey("file")) {
                                openFile(arg["file"].toString().replaceWithOrder(*args).translate(sender))
                            }
                            if (arg.containsKey("url")) {
                                openURL(arg["url"].toString().replaceWithOrder(*args).translate(sender))
                            }
                        }
                    }
                }
                if (index + 1 < json.size) {
                    newLine()
                }
            }
        }
    }
}