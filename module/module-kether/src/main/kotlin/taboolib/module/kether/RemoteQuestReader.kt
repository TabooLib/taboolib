package taboolib.module.kether

import taboolib.common.OpenContainer
import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader

class RemoteQuestReader(val remote: OpenContainer, val source: Any) : QuestReader {

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