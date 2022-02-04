package taboolib.module.configuration

import taboolib.common.LifeCycle
import taboolib.common.env.RuntimeDependency
import taboolib.common.inject.Injector
import taboolib.common.platform.Awake
import taboolib.common.platform.function.releaseResourceFile
import taboolib.common.platform.function.warning
import org.tabooproject.reflex.UnsafeAccess
import taboolib.common5.FileWatcher
import java.io.File
import java.lang.reflect.Field
import java.util.concurrent.CopyOnWriteArraySet
import java.util.function.Supplier

@RuntimeDependency("!org.yaml:snakeyaml:1.28", test = "!org.yaml.snakeyaml.Yaml")
@Awake
object ConfigLoader : Injector.Fields {

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
                if (field.getAnnotation(Config::class.java).migrate) {
                    val resourceAsStream = clazz.classLoader.getResourceAsStream(file.name)
                    if (resourceAsStream != null) {
                        val bytes = resourceAsStream.migrateTo(file.inputStream())
                        if (bytes != null) {
                            file.writeBytes(bytes)
                        }
                    }
                }
                val conf = SecuredFile.loadConfiguration(file)
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
                            conf.load(file)
                        }
                    }
                }
                val configFile = ConfigFile(conf, file)
                conf.onReload {
                    configFile.nodes.forEach { NodeLoader.inject(it, clazz, instance) }
                }
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
                val data = file.conf.get(node.value.ifEmpty { field.name })
                if (field.type == ConfigNodeTransfer::class.java) {
                    UnsafeAccess.get<ConfigNodeTransfer<*, *>>(instance.get(), field)!!.update(data)
                } else {
                    UnsafeAccess.put(instance.get(), field, data)
                }
            }
        }

        override val priority: Byte
            get() = 1

        override val lifeCycle: LifeCycle
            get() = LifeCycle.INIT
    }

    class ConfigFile(val conf: SecuredFile, val file: File) {

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