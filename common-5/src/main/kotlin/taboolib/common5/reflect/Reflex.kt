package taboolib.common5.reflect

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
        val deep = path.indexOf("/")
        if (deep == -1) {
            return get(path)
        }
        var find: T? = null
        var ref = of(get(path.substring(0, deep))!!)
        path.substring(deep).split("/").filter { it.isNotEmpty() }.forEach { point ->
            find = ref.get(point)
            if (find != null) {
                ref = of(find!!)
            }
        }
        return find
    }

    fun write(path: String, value: Any?) {
        val deep = path.indexOf("/")
        if (deep == -1) {
            return set(path, value)
        }
        val node0 = path.substring(0, deep)
        val node1 = path.substring(path.lastIndexOf("/") + 1, path.length)
        val space = path.substring(deep).split("/").filter { it.isNotEmpty() }
        var ref = of(get(node0)!!)
        space.forEachIndexed { index, point ->
            if (index + 1 < space.size) {
                ref = of(ref.get(point)!!)
            }
        }
        ref.set(node1, value)
    }

    fun <T> get(type: Class<T>, index: Int = 0): T? {
        val field = from.reflexFields().values.filter { it.type == type }.getOrNull(index - 1) ?: throw NoSuchFieldException("$type($index) at $from")
        val obj = Ref.getField(instance, field)
        return if (obj != null) obj as T else null
    }

    fun <T> get(name: String): T? {
        val map = from.reflexFields()
        val obj = Ref.getField(instance, map[name] ?: throw NoSuchFieldException("$name at $from"))
        return if (obj != null) obj as T else null
    }

    fun set(type: Class<*>, value: Any?, index: Int = 0) {
        val field = from.reflexFields().values.filter { it.type == type }.getOrNull(index - 1) ?: throw NoSuchFieldException("$type($index) at $from")
        Ref.putField(instance, field, value)
    }

    fun set(name: String, value: Any?) {
        val map = from.reflexFields()
        Ref.putField(instance, map[name] ?: throw NoSuchFieldException("$name at $from"), value)
    }

    fun <T> invoke(name: String, vararg parameter: Any?): T? {
        val map = from.reflexMethods()
        val method = map.filter { it.first == name }.firstOrNull {
            if (it.second.parameterCount == parameter.size) {
                var checked = true
                it.second.parameterTypes.forEachIndexed { index, p ->
                    if (parameter[index] != null && !p.isInstance(parameter[index])) {
                        checked = false
                    }
                }
                return@firstOrNull checked
            }
            return@firstOrNull false
        } ?: throw NoSuchMethodException("$name(${parameter.joinToString(", ") { it?.javaClass?.name ?: "null" }}) at $from")
        val obj = method.second.invoke(instance, *parameter)
        return if (obj != null) obj as T else null
    }

    private fun of(instance: Any): Reflex = instance.toReflex()

    companion object {

        private val fieldMap = ConcurrentHashMap<String, Map<String, Field>>()
        private val methodMap = ConcurrentHashMap<String, List<Pair<String, Method>>>()

        /**
         * 将对象转换为 Reflex 结构
         */
        fun Any.toReflex(clazz: Class<*>? = null) = Reflex(clazz ?: javaClass).instance(this)

        /**
         * 通过 Reflex 执行对象中的方法
         */
        fun <T> Any.reflexInvoke(path: String, vararg parameter: Any?) = toReflex().invoke<T>(path, *parameter)

        /**
         * 通过 Reflex 获取对象中的属性
         */
        fun <T> Any.reflex(path: String) = toReflex().read<T>(path)

        /**
         * 通过 Reflex 获取对象中的属性
         */
        fun Any.reflex(path: String, value: Any?) = toReflex().write(path, value)

        /**
         * 将 Class 转换 Reflex 结构
         */
        fun Class<*>.asReflex(instance: Any? = null) = Reflex(this).instance(instance)

        /**
         * 通过 Reflex 执行 Class 中的静态方法
         */
        fun <T> Class<*>.staticInvoke(path: String, vararg parameter: Any?) = asReflex().invoke<T>(path, *parameter)

        /**
         * 通过 Reflex 获取 Class 中的静态属性
         */
        fun <T> Class<*>.static(path: String) = asReflex().read<T>(path)

        /**
         * 通过 Reflex 设置 Class 中的静态属性
         */
        fun Class<*>.static(path: String, value: Any?) = asReflex().write(path, value)

        /**
         * 缓存并获取所有属性（包含父类）
         */
        fun Class<*>.reflexFields() = fieldMap.computeIfAbsent(name) {
            HashMap<String, Field>().also { map ->
                fun cache(clazz: Class<*>) {
                    map.putAll(clazz.declaredFields.map {
                        it.isAccessible = true
                        it.name to it
                    })
                    if (clazz.superclass != null && clazz.superclass != Object::class.java) {
                        cache(clazz.superclass)
                    }
                }
                cache(this)
            }
        }

        /**
         * 缓存并获取所有方法（包含父类及接口）
         */
        fun Class<*>.reflexMethods() = methodMap.computeIfAbsent(name) {
            ArrayList<Pair<String, Method>>().also { map ->
                fun cache(clazz: Class<*>) {
                    map.addAll(clazz.declaredMethods.map {
                        it.isAccessible = true
                        it.name to it
                    })
                    if (clazz.superclass != null && clazz.superclass != Object::class.java) {
                        cache(clazz.superclass)
                    }
                    clazz.interfaces.forEach {
                        cache(it)
                    }
                }
                cache(this)
            }
        }
    }
}