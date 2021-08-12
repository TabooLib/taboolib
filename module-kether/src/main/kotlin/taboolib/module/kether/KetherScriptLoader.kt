package taboolib.module.kether

import taboolib.common.OpenContainer
import taboolib.common.reflect.Reflex.Companion.getProperty
import taboolib.common.reflect.Reflex.Companion.invokeMethod
import taboolib.common.reflect.Reflex.Companion.setProperty
import taboolib.library.kether.*
import taboolib.library.kether.actions.GetAction
import taboolib.library.kether.actions.LiteralAction
import taboolib.module.kether.action.ActionProperty
import java.util.*

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

//        companion object {
//
//            @Suppress("UNCHECKED_CAST")
//            fun deserialize(map: Map<String, Any>): Reader {
//                return Reader(
//                    ServiceHolder.getQuestServiceInstance(),
//                    BlockReader.deserialize(map["parser"] as MutableMap<String, Any>),
//                    map["namespace"] as MutableList<String>
//                )
//            }
//        }

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

    class RemoteReader(val remote: OpenContainer, val source: Any): QuestReader {

        override fun peek(): Char {
            return source.invokeMethod("peek")!!
        }

        override fun peek(n: Int): Char {
            return source.invokeMethod("peek", n)!!
        }

        override fun getIndex(): Int {
            return source.invokeMethod("getIndex")!!
        }

        override fun getMark(): Int {
            return source.invokeMethod("getMark")!!
        }

        override fun hasNext(): Boolean {
            return source.invokeMethod("hasNext")!!
        }

        override fun nextToken(): String {
            return source.invokeMethod("nextToken")!!
        }

        override fun mark() {
            source.invokeMethod<Void>("mark")
        }

        override fun reset() {
            source.invokeMethod<Void>("reset")
        }

        override fun <T> nextAction(): ParsedAction<T> {
            val action = source.invokeMethod<T>("nextAction")!!
            return ParsedAction(RemoteQuestAction<T>(remote, action.getProperty<Any>("action")!!), action.getProperty<Map<String, Any>>("properties")!!)
        }

        override fun expect(value: String) {
            source.invokeMethod<Void>("expect", value)
        }
    }
}