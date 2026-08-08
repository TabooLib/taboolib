package taboolib.module.kether

import org.tabooproject.reflex.ClassMethod
import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import org.tabooproject.reflex.ReflexClass
import taboolib.common.OpenContainer
import taboolib.common.util.supplierLazy
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader

/**
 * 远程脚本读取器。
 *
 * 真正被共享的读取游标位于 [source] 上，因此所有读取操作都以 [source] 为锁对象串行化。
 * 若锁 Reader 实例本身，两个包装同一 [source] 的 Reader 之间将完全不互斥，竞态依旧存在。
 */
@Suppress("UNCHECKED_CAST")
class RemoteQuestReader(val remote: OpenContainer, val source: Any) : QuestReader {

    override fun peek(): Char = synchronized(source) {
        return source.invokeMethod("peek", remap = false)!!
    }

    override fun peek(n: Int): Char = synchronized(source) {
        return peekIntMethod[source].invoke(source, n) as Char
    }

    override fun getIndex(): Int = synchronized(source) {
        return source.invokeMethod("getIndex", remap = false)!!
    }

    override fun getMark(): Int = synchronized(source) {
        return source.invokeMethod("getMark", remap = false)!!
    }

    override fun hasNext(): Boolean = synchronized(source) {
        return source.invokeMethod("hasNext", remap = false)!!
    }

    override fun nextToken(): String = synchronized(source) {
        return source.invokeMethod("nextToken", remap = false)!!
    }

    override fun mark() = synchronized(source) {
        source.invokeMethod<Void>("mark", remap = false)
        Unit
    }

    override fun reset() = synchronized(source) {
        source.invokeMethod<Void>("reset", remap = false)
        Unit
    }

    override fun <T> nextAction(): ParsedAction<T> = synchronized(source) {
        val action = source.invokeMethod<T>("nextAction", remap = false)!!
        val questAction = RemoteQuestAction<T>(remote, action.getProperty<Any>("action", remap = false)!!)
        return ParsedAction(questAction, action.getProperty<Map<String, Any>>("properties", remap = false)!!)
    }

    override fun <T : Any?> nextAction(namespace: String?): ParsedAction<T> = synchronized(source) {
        return try {
            val action = nextActionStringMethod[source].invoke(source, namespace)!!
            val questAction = RemoteQuestAction<T>(remote, action.getProperty<Any>("action", remap = false)!!)
            ParsedAction(questAction, action.getProperty<Map<String, Any>>("properties", remap = false)!!)
        } catch (_: NoSuchMethodException) {
            // 注意：synchronized 为可重入锁，此处回退调用不会自锁
            nextAction()
        }
    }

    override fun expect(value: String) = synchronized(source) {
        expectMethod[source].invoke(source, value)
        Unit
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
