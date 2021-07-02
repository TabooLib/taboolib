package taboolib.module.kether

import java.io.Serializable
import kotlin.reflect.KClass

/**
 * @Author sky
 * @Since 2020-08-30 19:22
 */
class EventOperator<T : Any>(val event: KClass<out T>) : Serializable {

    private val serialVersionUID = 1L

    val reader = HashMap<String, Reader<T>>()
    val writer = HashMap<String, Writer<T>>()

    fun unit(name: String, builder: Builder<T>.() -> Unit): EventOperator<T> {
        Builder(name, this).also {
            builder(it)
            reader[name] = Reader(it.reader)
            writer[name] = Writer(it.writer)
        }
        return this
    }

    @Suppress("UNCHECKED_CAST")
    fun read(name: String, event: Any): Any? {
        val reader = reader[name] as? Reader<Any> ?: throw IllegalStateException("Operator \"$name\" not supported.")
        return reader.func(event)
    }

    @Suppress("UNCHECKED_CAST")
    fun readUnsafe(name: String, event: Any): Any? {
        val reader = reader[name] as? Reader<Any> ?: return null
        return reader.func(event)
    }

    @Suppress("UNCHECKED_CAST")
    fun write(name: String, event: Any, value: Any?) {
        val writer = writer[name] as? Writer<Any> ?: throw IllegalStateException("Operator \"$name\" not supported.")
        writer.func(event, value)
    }

    @Suppress("UNCHECKED_CAST")
    fun writeUnsafe(name: String, event: Any, value: Any?) {
        val writer = writer[name] as? Writer<Any> ?: return
        writer.func(event, value)
    }

    class Reader<T : Any>(val func: (T) -> Any?)

    class Writer<T : Any>(val func: (T, Any?) -> Unit)

    class Builder<T : Any>(val name: String, val event: EventOperator<T>) {

        var reader: (T) -> Any? = { null }
        var writer: (T, Any?) -> Unit = { _, _ -> }
    }
}