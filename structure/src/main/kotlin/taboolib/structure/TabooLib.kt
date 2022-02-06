package taboolib.structure

object TabooLib {

    val main = Group {
        add("common-adapter") {
            platform()
            dependency("common-core")
        }
        add("common-command") {
            platform()
            dependency("common-adapter")
        }
        add("common-command-annotation") {
            dependency("common-command")
        }
        add("common-core") {
            platform()
        }
        add("common-core-impl") {
            dependency("common-core")
        }
        add("common-environment") {
            dependency("common-core")
        }
        add("common-event") {
            platform()
            dependency("common-core")
        }
        add("common-listener") {
            platform()
            dependency("common-core")
        }
        add("common-openapi") {
            platform()
            dependency("common-core")
        }
        add("common-plugin") {
            platform()
            dependency("common-core")
        }
        add("common-scheduler") {
            platform()
            dependency("common-core")
        }
        add("common-util") {
            dependency("common-environment")
            dependency("common-plugin").optional()
            dependency("common-adapter").optional()
        }
        add("common-util-shaded") {
            dependency("common-core")
            dependency("common-plugin").optional()
            dependency("common-adapter").optional()
        }
    }
}

class Group(init: Group.() -> Unit) {

    val modules = ArrayList<Module>()

    init {
        init()
    }

    fun add(name: String, init: Module.() -> Unit = {}) {
        modules.add(Module(name, this).also(init))
    }

    fun get(name: String): Module? {
        return modules.find { it.name == name }
    }

    class Module(val name: String, val group: Group) {

        val dependencies = ArrayList<Dependency>()
        var platform = false

        fun platform(): Module {
            platform = true
            return this
        }

        fun dependency(name: String): Dependency {
            return Dependency(name).apply { dependencies.add(this) }
        }

        fun collect(): Set<Dependency> {
            val result = HashSet<Dependency>()
            result.addAll(dependencies)
            dependencies.forEach { group.get(it.name)?.collect()?.apply { result.addAll(this) } }
            return result
        }
    }
}

class Dependency(val name: String) {

    var optional: Boolean = false

    fun optional(): Dependency {
        optional = true
        return this
    }
}