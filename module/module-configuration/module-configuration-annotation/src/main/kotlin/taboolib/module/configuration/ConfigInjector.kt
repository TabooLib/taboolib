package taboolib.module.configuration

import org.tabooproject.reflex.UnsafeAccess
import taboolib.common.LifeCycle
import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency
import taboolib.common.inject.Injector
import taboolib.common.platform.Awake
import taboolib.common.platform.function.releaseResourceFile
import taboolib.common.platform.function.warning
import taboolib.common.util.nonPrimitive
import taboolib.common5.Coerce
import taboolib.common5.FileWatcher
import java.io.File
import java.lang.reflect.Field
import java.util.concurrent.CopyOnWriteArraySet
import java.util.function.Supplier

@Awake
object ConfigInjector : Injector(LifeCycle.INIT) {

    val files = HashMap<String, ConfigFile>()

    override fun inject(field: Field, clazz: Class<*>, instance: Supplier<*>) {
        if (field.isAnnotationPresent(Config::class.java)) {
            val name = field.getAnnotation(Config::class.java).value
            if (files.containsKey(name)) {
                try {
                    // ClassCastException
                    UnsafeAccess.put(instance.get(), field, files[name]!!.conf)
                } catch (ex: Throwable) {
                    ex.printStackTrace()
                    return
                }
            } else {
                val file = releaseResourceFile(name)
                val conf = Configuration.loadFromFile(file)
                try {
                    // ClassCastException
                    UnsafeAccess.put(instance.get(), field, conf)
                } catch (ex: Throwable) {
                    ex.printStackTrace()
                    return
                }
                if (field.getAnnotation(Config::class.java).autoReload && isFileWatcherHook) {
                    FileWatcher.INSTANCE.addSimpleListener(file) {
                        if (file.exists()) {
                            conf.loadFromFile(file)
                        }
                    }
                }
                val configFile = ConfigFile(conf, file)
                conf.onReload { configFile.nodes.forEach { NodeLoader.inject(it, clazz, instance) } }
                files[name] = configFile
            }
        }
    }

    @Awake
    object NodeLoader : Injector.Fields {

        override fun inject(field: Field, clazz: Class<*>, instance: Supplier<*>) {
            if (field.isAnnotationPresent(ConfigNode::class.java)) {
                val node = field.getAnnotation(ConfigNode::class.java)
                val file = files[node.bind]
                if (file == null) {
                    warning("${node.bind} not defined")
                    return
                }
                file.nodes += field
                var data = file.conf[node.value.ifEmpty { field.name }]
                if (field.type == ConfigNodeTransfer::class.java) {
                    UnsafeAccess.get<ConfigNodeTransfer<*, *>>(instance.get(), field)!!.update(data)
                } else {
                    when (field.type.nonPrimitive()) {
                        Integer::class.java -> data = Coerce.toInteger(data)
                        Character::class.java -> data = Coerce.toChar(data)
                        java.lang.Byte::class.java -> data = Coerce.toByte(data)
                        java.lang.Long::class.java -> data = Coerce.toLong(data)
                        java.lang.Double::class.java -> data = Coerce.toDouble(data)
                        java.lang.Float::class.java -> data = Coerce.toFloat(data)
                        java.lang.Short::class.java -> data = Coerce.toShort(data)
                        java.lang.Boolean::class.java -> data = Coerce.toBoolean(data)
                    }
                    UnsafeAccess.put(instance.get(), field, data)
                }
            }
        }

        override val priority: Byte
            get() = 1

        override val lifeCycle: LifeCycle
            get() = LifeCycle.INIT
    }

    class ConfigFile(val conf: Configuration, val file: File) {

        val nodes = CopyOnWriteArraySet<Field>()
    }

    val isFileWatcherHook by lazy {
        try {
            FileWatcher.INSTANCE
            true
        } catch (ex: NoClassDefFoundError) {
            false
        }
    }
}