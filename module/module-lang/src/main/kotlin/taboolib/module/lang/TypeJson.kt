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
    var jsonArgs = ArrayList<Map<String, Any>>()

    override fun init(source: Map<String, Any>) {
        text = source["text"]?.asList()
        try {
            jsonArgs.addAll((source["args"] as List<*>).map { (it as Map<*, *>).map { (k, v) -> k.toString() to v!! }.toMap() })
        } catch (_: ClassCastException) {
        }
    }

    override fun send(sender: ProxyCommandSender, vararg args: Any) {
        TellrawJson().sendTo(sender) {
            var i = 0
            text?.forEachIndexed { index, line ->
                parser.readToFlatten(line).forEach { part ->
                    append(part.text.translate(sender).replaceWithOrder(*args))
                    if (part.isVariable) {
                        val arg = jsonArgs.getOrNull(i++)
                        if (arg != null) {
                            if (arg.containsKey("hover")) {
                                hoverText(arg["hover"].toString().translate(sender).replaceWithOrder(*args))
                            }
                            if (arg.containsKey("command")) {
                                runCommand(arg["command"].toString().translate(sender).replaceWithOrder(*args))
                            }
                            if (arg.containsKey("suggest")) {
                                suggestCommand(arg["suggest"].toString().translate(sender).replaceWithOrder(*args))
                            }
                            if (arg.containsKey("insertion")) {
                                insertion(arg["insertion"].toString().translate(sender).replaceWithOrder(*args))
                            }
                            if (arg.containsKey("copy")) {
                                copyToClipboard(arg["copy"].toString().translate(sender).replaceWithOrder(*args))
                            }
                            if (arg.containsKey("file")) {
                                openFile(arg["file"].toString().translate(sender).replaceWithOrder(*args))
                            }
                            if (arg.containsKey("url")) {
                                openURL(arg["url"].toString().translate(sender).replaceWithOrder(*args))
                            }
                        }
                    }
                }
                if (index + 1 < text!!.size) {
                    newLine()
                }
            }
        }
    }

    override fun send(sender: ProxyCommandSender, func: (String?) -> String?) {
        TellrawJson().sendTo(sender) {
            var i = 0
            text?.forEachIndexed { index, line ->
                parser.readToFlatten(line).forEach { part ->
                    append(func.invoke(part.text.translate(sender))!!)
                    if (part.isVariable) {
                        val arg = jsonArgs.getOrNull(i++)
                        if (arg != null) {
                            if (arg.containsKey("hover")) {
                                hoverText(func.invoke(arg["hover"].toString().translate(sender))!!)
                            }
                            if (arg.containsKey("command")) {
                                runCommand(func.invoke(arg["command"].toString().translate(sender))!!)
                            }
                            if (arg.containsKey("suggest")) {
                                suggestCommand(func.invoke(arg["suggest"].toString().translate(sender))!!)
                            }
                            if (arg.containsKey("insertion")) {
                                insertion(func.invoke(arg["insertion"].toString().translate(sender))!!)
                            }
                            if (arg.containsKey("copy")) {
                                copyToClipboard(func.invoke(arg["copy"].toString().translate(sender))!!)
                            }
                            if (arg.containsKey("file")) {
                                openFile(func.invoke(arg["file"].toString().translate(sender))!!)
                            }
                            if (arg.containsKey("url")) {
                                openURL(func.invoke(arg["url"].toString().translate(sender))!!)
                            }
                        }
                    }
                }
                if (index + 1 < text!!.size) {
                    newLine()
                }
            }
        }
    }

    companion object {

        private val parser = VariableReader("[", "]")
    }
}