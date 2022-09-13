package taboolib.module.kether

import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import taboolib.common.OpenContainer
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader

class RemoteQuestReader(val remote: OpenContainer, val source: Any) : QuestReader {

    override fun peek(): Char {
        return source.invokeMethod("peek", remap = false)!!
    }

    override fun peek(n: Int): Char {
        return source.invokeMethod("peek", n, remap = false)!!
    }

    override fun getIndex(): Int {
        return source.invokeMethod("getIndex", remap = false)!!
    }

    override fun getMark(): Int {
        return source.invokeMethod("getMark", remap = false)!!
    }

    override fun hasNext(): Boolean {
        return source.invokeMethod("hasNext", remap = false)!!
    }

    override fun nextToken(): String {
        return source.invokeMethod("nextToken", remap = false)!!
    }

    override fun mark() {
        source.invokeMethod<Void>("mark", remap = false)
    }

    override fun reset() {
        source.invokeMethod<Void>("reset", remap = false)
    }

    override fun <T> nextAction(): ParsedAction<T> {
        val action = source.invokeMethod<T>("nextAction", remap = false)!!
        val questAction = RemoteQuestAction<T>(remote, action.getProperty<Any>("action", remap = false)!!)
        return ParsedAction(questAction, action.getProperty<Map<String, Any>>("properties")!!)
    }

    override fun expect(value: String) {
        source.invokeMethod<Void>("expect", value, remap = false)
    }
}