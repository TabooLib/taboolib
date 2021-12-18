package taboolib.module.kether

import taboolib.common.reflect.Reflex.Companion.setProperty
import taboolib.library.kether.*
import taboolib.library.kether.actions.LiteralAction
import taboolib.module.kether.action.ActionGet
import taboolib.module.kether.action.ActionProperty

/**
 * TabooLib
 * taboolib.module.kether.KetherScriptLoader
 *
 * @author sky
 * @since 2021/7/26 2:35 下午
 */
class KetherScriptLoader : SimpleQuestLoader() {

    override fun newBlockReader(content: CharArray, service: QuestService<*>, namespace: MutableList<String>): BlockReader {
        return object : BlockReader(content, service, namespace) {

            override fun newActionReader(service: QuestService<*>, namespace: MutableList<String>): SimpleReader {
                return Reader(service, this, namespace)
            }
        }
    }

    class Reader(service: QuestService<*>, reader: BlockReader, namespace: MutableList<String>) : SimpleReader(service, reader, namespace) {

        override fun nextToken(): String {
            return super.nextToken().replace("\\s", " ")
        }

        @Suppress("UNCHECKED_CAST")
        override fun <T : Any?> nextAction(): ParsedAction<T> {
            skipBlank()
            return when (peek()) {
                '{' -> {
                    blockParser.setProperty("index", index)
                    val action = nextAnonAction()
                    index = blockParser.index
                    action as ParsedAction<T>
                }
                '&' -> {
                    skip(1)
                    val token = nextToken()
                    if (token.isNotEmpty() && token[token.length - 1] == ']' && token.indexOf('[') in 1 until token.length) {
                        val i = token.indexOf('[')
                        wrap(ActionProperty.Get(wrap(ActionGet<Any>(token.substring(0, i))), token.substring(i + 1, token.length - 1))) as ParsedAction<T>
                    } else {
                        wrap(ActionGet(token))
                    }
                }
                '*' -> {
                    skip(1)
                    wrap(LiteralAction(nextToken()))
                }
                else -> {
                    // property player[name]
                    val token = nextToken()
                    if (token.isNotEmpty() && token[token.length - 1] == ']' && token.indexOf('[') in 1 until token.length) {
                        val i = token.indexOf('[')
                        val element = token.substring(0, i)
                        val optional = service.registry.getParser(element, namespace)
                        if (optional.isPresent) {
                            val propertyKey = token.substring(i + 1, token.length - 1)
                            return wrap(ActionProperty.Get(wrap(optional.get().resolve<Any>(this)), propertyKey)) as ParsedAction<T>
                        } else if (Kether.isAllowToleranceParser) {
                            val propertyKey = token.substring(i + 1, token.length - 1)
                            return wrap(ActionProperty.Get(wrap(LiteralAction<Any>(element)), propertyKey)) as ParsedAction<T>
                        }
                        throw LoadError.UNKNOWN_ACTION.create(element)
                    } else {
                        val optional = service.registry.getParser(token, namespace)
                        if (optional.isPresent) {
                            return wrap(optional.get().resolve(this))
                        } else if (Kether.isAllowToleranceParser) {
                            return wrap(LiteralAction(token))
                        }
                        throw LoadError.UNKNOWN_ACTION.create(token)
                    }
                }
            }
        }
    }
}