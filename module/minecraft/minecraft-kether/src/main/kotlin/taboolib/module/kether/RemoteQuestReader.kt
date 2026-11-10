package taboolib.module.kether

import org.tabooproject.reflex.ClassMethod
import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import org.tabooproject.reflex.ReflexClass
import taboolib.common.OpenContainer
import taboolib.common.util.supplierLazy
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader

@Suppress("UNCHECKED_CAST")
class RemoteQuestReader(val remote: OpenContainer, val source: Any) : QuestReader {

    @Synchronized
    override fun peek(): Char {
        return source.invokeMethod("peek", remap = false)!!
    }

    @Synchronized
    override fun peek(n: Int): Char {
        return peekIntMethod[source].invoke(source, n) as Char
    }

    @Synchronized
    override fun getIndex(): Int {
        return source.invokeMethod("getIndex", remap = false)!!
    }

    @Synchronized
    override fun getMark(): Int {
        return source.invokeMethod("getMark", remap = false)!!
    }

    @Synchronized
    override fun hasNext(): Boolean {
        return source.invokeMethod("hasNext", remap = false)!!
    }

    @Synchronized
    override fun nextToken(): String {
        return source.invokeMethod("nextToken", remap = false)!!
    }

    @Synchronized
    override fun mark() {
        source.invokeMethod<Void>("mark", remap = false)
    }

    @Synchronized
    override fun reset() {
        source.invokeMethod<Void>("reset", remap = false)
    }

    @Synchronized
    override fun <T> nextAction(): ParsedAction<T> {
        val action = source.invokeMethod<T>("nextAction", remap = false)!!
        val questAction = RemoteQuestAction<T>(remote, action.getProperty<Any>("action", remap = false)!!)
        return ParsedAction(questAction, action.getProperty<Map<String, Any>>("properties", remap = false)!!)
    }

    @Synchronized
    override fun <T : Any?> nextAction(namespace: String?): ParsedAction<T> {
        return try {
            val action = nextActionStringMethod[source].invoke(source, namespace)!!
            val questAction = RemoteQuestAction<T>(remote, action.getProperty<Any>("action", remap = false)!!)
            ParsedAction(questAction, action.getProperty<Map<String, Any>>("properties", remap = false)!!)
        } catch (_: NoSuchMethodException) {
            nextAction()
        }
    }

    @Synchronized
    override fun expect(value: String) {
        expectMethod[source].invoke(source, value)
    }

    companion object {

        val peekIntMethod = supplierLazy<Any, ClassMethod>(typeIsolation = true) {
            ReflexClass.of(it.javaClass).getMethodByTypes("peek", remap = false, parameter = arrayOf(Int::class.java))
        }

        val nextActionStringMethod = supplierLazy<Any, ClassMethod>(typeIsolation = true) {
            ReflexClass.of(it.javaClass).getMethodByTypes("nextAction", remap = false, parameter = arrayOf(String::class.java))
        }

        val expectMethod = supplierLazy<Any, ClassMethod>(typeIsolation = true) {
            ReflexClass.of(it.javaClass).getMethodByTypes("expect", remap = false, parameter = arrayOf(String::class.java))
        }
    }
}