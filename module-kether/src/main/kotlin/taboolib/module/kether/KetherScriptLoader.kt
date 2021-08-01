package taboolib.module.kether

import io.izzel.kether.common.actions.GetAction
import io.izzel.kether.common.actions.LiteralAction
import io.izzel.kether.common.api.ParsedAction
import io.izzel.kether.common.api.QuestService
import io.izzel.kether.common.loader.LoadError
import io.izzel.kether.common.loader.SimpleQuestLoader
import io.izzel.kether.common.loader.SimpleReader
import taboolib.common.reflect.Reflex.Companion.setProperty
import taboolib.module.kether.action.ActionProperty

/**
 * TabooLib
 * taboolib.module.kether.KetherScriptLoader
 *
 * @author sky
 * @since 2021/7/26 2:35 下午
 */
class KetherScriptLoader : SimpleQuestLoader() {

    override fun newParser(content: CharArray, service: QuestService<*>, namespace: MutableList<String>): Parser {
        return KetherScriptParser(content, service, namespace)
    }

    class KetherScriptParser(content: CharArray, service: QuestService<*>, namespace: MutableList<String>) : Parser(content, service, namespace) {

        override fun newReader(service: QuestService<*>, namespace: MutableList<String>): SimpleReader {
            return KetherScriptReader(service, this, namespace)
        }
    }

    class KetherScriptReader(service: QuestService<*>, parser: Parser, namespace: MutableList<String>) : SimpleReader(service, parser, namespace) {

        override fun nextToken(): String {
            return super.nextToken().replace("\\s", " ")
        }

        @Suppress("UNCHECKED_CAST")
        override fun <T : Any?> nextAction(): ParsedAction<T> {
            skipBlank()
            return when (peek()) {
                '{' -> {
                    parser.setProperty("index", index)
                    val action = nextAnonAction()
                    index = parser.index
                    action as ParsedAction<T>
                }
                '&' -> {
                    skip(1)
                    val token = nextToken()
                    if (token.isNotEmpty() && token[token.length - 1] == ']' && token.indexOf('[') in 1 until token.length) {
                        val i = token.indexOf('[')
                        wrap(ActionProperty.Get(wrap(GetAction<Any>(token.substring(0, i))), token.substring(i + 1, token.length - 1))) as ParsedAction<T>
                    } else {
                        wrap(GetAction(token))
                    }
                }
                '*' -> {
                    skip(1)
                    wrap(LiteralAction(nextToken()))
                }
                else -> {
                    val token = nextToken()
                    if (token.isNotEmpty() && token[token.length - 1] == ']' && token.indexOf('[') in 1 until token.length) {
                        val i = token.indexOf('[')
                        val element = token.substring(0, i)
                        val optional = service.registry.getParser(element, namespace)
                        if (optional.isPresent) {
                            val propertyKey = token.substring(i + 1, token.length - 1)
                            return wrap(ActionProperty.Get(wrap(optional.get().resolve<Any>(this)), propertyKey)) as ParsedAction<T>
                        }
                        throw LoadError.UNKNOWN_ACTION.create(element)
                    } else {
                        val optional = service.registry.getParser(token, namespace)
                        if (optional.isPresent) {
                            return wrap(optional.get().resolve(this))
                        }
                        throw LoadError.UNKNOWN_ACTION.create(token)
                    }
                }
            }
        }
    }
}