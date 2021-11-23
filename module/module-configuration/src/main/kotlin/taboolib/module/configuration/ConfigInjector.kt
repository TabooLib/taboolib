package taboolib.module.configuration

import taboolib.common.LifeCycle
import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency
import taboolib.common.inject.Injector
import taboolib.common.platform.Awake
import taboolib.common.platform.function.releaseResourceFile
import taboolib.common.platform.function.warning
import taboolib.common.reflect.Ref
import taboolib.common.util.nonPrimitive
import taboolib.common5.Coerce
import taboolib.common5.FileWatcher
import java.io.File
import java.lang.reflect.Field
import java.util.concurrent.CopyOnWriteArraySet
import java.util.function.Supplier

@RuntimeDependencies(
    RuntimeDependency("!com.typesafe:config:1.4.1", test = "!com.typesafe.config.Config"),
    RuntimeDependency("!com.amihaiemil.web:eo-yaml:6.0.0", test = "!com.amihaiemil.eoyaml.Yaml"),
    RuntimeDependency("!com.electronwill.night-config:core:3.6.5", test = "!com.electronwill.nightconfig.core.Config"),
    RuntimeDependency("!com.electronwill.night-config:toml:3.6.5", test = "!com.electronwill.nightconfig.toml.TomlFormat"),
    RuntimeDependency("!com.electronwill.night-config:json:3.6.5", test = "!com.electronwill.nightconfig.toml.JsonFormat"),
    RuntimeDependency("!com.electronwill.night-config:hocon:3.6.5", test = "!com.electronwill.nightconfig.toml.HoconFormat")
)
@Awake
object ConfigInjector : Injector.Fields {

    val files = HashMap<String, ConfigFile>()

    override fun inject(field: Field, clazz: Class<*>, instance: Supplier<*>) {
        if (field.isAnnotationPresent(Config::class.java)) {
            val name = field.getAnnotation(Config::class.java).value
            if (files.containsKey(name)) {
                try {
                    // ClassCastException
                    Ref.put(instance.get(), field, files[name]!!.conf)
                } catch (ex: Throwable) {
                    ex.printStackTrace()
                    return
                }
            } else {
                val file = releaseResourceFile(name)
                // 2021/11/22 停止支持
//                if (field.getAnnotation(Config::class.java).migrate) {
//                    val resourceAsStream = clazz.classLoader.getResourceAsStream(file.name)
//                    if (resourceAsStream != null) {
//                        val bytes = resourceAsStream.migrateTo(file.inputStream())
//                        if (bytes != null) {
//                            file.writeBytes(bytes)
//                        }
//                    }
//                }
                val conf = if (field.type == SecuredFile::class.java) SecuredFile.loadConfiguration(file) else Configuration.loadFromFile(file)
                try {
                    // ClassCastException
                    Ref.put(instance.get(), field, conf)
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

    override val priority: Byte
        get() = 0

    override val lifeCycle: LifeCycle
        get() = LifeCycle.INIT

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
                    Ref.get<ConfigNodeTransfer<*, *>>(instance.get(), field)!!.update(data)
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
                    Ref.put(instance.get(), field, data)
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