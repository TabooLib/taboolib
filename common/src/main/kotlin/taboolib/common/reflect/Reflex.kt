package taboolib.common.reflect

import taboolib.common.reflect.Reflex.Companion.reflexInvoke
import taboolib.common.reflect.Reflex.Companion.staticInvoke
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

/**
 * @author sky
 * @since 2020-10-02 01:40
 */
@Suppress("UNCHECKED_CAST")
class Reflex(val from: Class<*>) {

    var instance: Any? = null

    fun instance(instance: Any?): Reflex {
        this.instance = instance
        return this
    }

    fun <T> read(path: String): T? {
        val deep = path.indexOf('/')
        if (deep == -1) {
            return get(path)
        }
        var find: T? = null
        var ref = of(get(path.substring(0, deep))!!)
        path.substring(deep).split('/').filter { it.isNotEmpty() }.forEach { point ->
            find = ref.get(point)
            if (find != null) {
                ref = of(find!!)
            }
        }
        return find
    }

    fun write(path: String, value: Any?) {
        val deep = path.indexOf('/')
        if (deep == -1) {
            return set(path, value)
        }
        val node0 = path.substring(0, deep)
        val node1 = path.substring(path.lastIndexOf('/') + 1, path.length)
        val space = path.substring(deep).split('/').filter { it.isNotEmpty() }
        var ref = of(get(node0)!!)
        space.forEachIndexed { index, point ->
            if (index + 1 < space.size) {
                ref = of(ref.get(point)!!)
            }
        }
        ref.set(node1, value)
    }

    fun <T> get(type: Class<T>, index: Int = 0): T? {
        val field = ReflexClass.find(from).savingFields.filter { it.type == type }.getOrNull(index - 1)
        return Ref.get<T>(instance, field ?: throw NoSuchFieldException("$type($index) at $from"))
    }

    fun <T> get(name: String, def: T): T {
        return get(name) ?: def
    }

    fun <T> get(name: String): T? {
        return Ref.get<T>(instance, ReflexClass.find(from).findField(name) ?: throw NoSuchFieldException("$name at $from"))
    }

    fun set(type: Class<*>, value: Any?, index: Int = 0) {
        val field = ReflexClass.find(from).savingFields.filter { it.type == type }.getOrNull(index - 1)
        Ref.put(instance, field ?: throw NoSuchFieldException("$type($index) at $from"), value)
    }

    fun set(name: String, value: Any?) {
        Ref.put(instance, ReflexClass.find(from).findField(name) ?: throw NoSuchFieldException("$name at $from"), value)
    }

    fun <T> invoke(name: String, vararg parameter: Any?): T? {
        val map = ReflexClass.find(from).findMethod(name, *parameter) ?: throw NoSuchMethodException("$name(${parameter.joinToString(", ") { it?.javaClass?.name.toString() }}) at $from")
        val obj = map.invoke(instance, *parameter)
        return if (obj != null) obj as T else null
    }

    private fun of(instance: Any) = Reflex(instance.javaClass).instance(instance)

    companion object {

        val remapper = ArrayList<ReflexRemapper>()

        /**
         * 通过 Reflex 执行对象中的方法
         */
        fun <T> Any.reflexInvoke(path: String, vararg parameter: Any?) = Reflex(javaClass).instance(this).invoke<T>(path, *parameter)

        /**
         * 通过 Reflex 获取对象中的属性
         */
        fun <T> Any.reflex(path: String) = Reflex(javaClass).instance(this).read<T>(path)

        /**
         * 通过 Reflex 获取对象中的属性
         */
        fun Any.reflex(path: String, value: Any?) = Reflex(javaClass).instance(this).write(path, value)

        /**
         * 通过 Reflex 执行 Class 中的静态方法
         */
        fun <T> Class<*>.staticInvoke(path: String, vararg parameter: Any?) = Reflex(this).invoke<T>(path, *parameter)

        /**
         * 通过 Reflex 获取 Class 中的静态属性
         */
        fun <T> Class<*>.static(path: String) = Reflex(this).read<T>(path)

        /**
         * 通过 Reflex 设置 Class 中的静态属性
         */
        fun Class<*>.static(path: String, value: Any?) = Reflex(this).write(path, value)
    }
}