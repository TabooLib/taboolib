package taboolib.common.reflect

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

        var bukkit = true

        fun <T> Class<T>.unsafeInstance(): Any {
            return Ref.unsafe.allocateInstance(this)!!
        }

        fun <T> Any.invokeMethod(path: String, vararg parameter: Any?, fixed: Boolean = false): T? {
            return if (fixed && this is Class<*>) {
                Reflex(this).invoke(path, *parameter)
            } else {
                Reflex(javaClass).instance(this).invoke(path, *parameter)
            }
        }

        fun <T> Any.getProperty(path: String, fixed: Boolean = false): T? {
            return if (fixed && this is Class<*>) {
                Reflex(this).read(path)
            } else {
                Reflex(javaClass).instance(this).read(path)
            }
        }

        fun Any.setProperty(path: String, value: Any?, fixed: Boolean = false) {
            return if (fixed && this is Class<*>) {
                Reflex(this).write(path, value)
            } else {
                Reflex(javaClass).instance(this).write(path, value)
            }
        }

        init {
            try {
                Class.forName("org.bukkit.Bukkit")
            } catch (e: ClassNotFoundException) {
                bukkit = false
            }
        }
    }
}