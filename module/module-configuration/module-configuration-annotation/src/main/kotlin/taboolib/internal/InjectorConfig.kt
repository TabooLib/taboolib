package taboolib.internal

import org.tabooproject.reflex.ClassField
import taboolib.common.LifeCycle
import taboolib.common.inject.Bind
import taboolib.common.inject.Injector
import taboolib.common.io.InstGetter
import taboolib.common.platform.Awake
import taboolib.common.platform.function.releaseResourceFile
import taboolib.common5.FileWatcher
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import java.io.File
import java.util.concurrent.CopyOnWriteArraySet

@Internal
@Awake
@Bind([Config::class], target = Bind.Target.FIELD)
object InjectorConfig : Injector(LifeCycle.INIT) {

    val configFileMap = HashMap<String, ConfigFile>()
    val isFileWatcher = kotlin.runCatching { FileWatcher.INSTANCE != null }.getOrElse { false }

    override fun inject(clazz: Class<*>, field: ClassField, instance: InstGetter<*>) {
        val annotation = field.getAnnotation(Config::class.java)!!
        val name = annotation.property<String>("value")!!
        if (configFileMap.containsKey(name)) {
            try {
                field.set(instance.get(), configFileMap[name]!!.configuration)
            } catch (ex: Throwable) {
                ex.printStackTrace()
            }
        } else {
            val file = releaseResourceFile(name)
            val conf = Configuration.loadFromFile(file)
            try {
                field.set(instance.get(), conf)
            } catch (ex: Throwable) {
                ex.printStackTrace()
                return
            }
            if (annotation.property("autoload")!! && isFileWatcher) {
                FileWatcher.INSTANCE.addSimpleListener(file) {
                    if (file.exists()) {
                        conf.loadFromFile(file)
                    }
                }
            }
            val configFile = ConfigFile(conf, file)
            conf.onReload { configFile.nodes.forEach { InjectorConfigNode.inject(clazz, it, instance) } }
            configFileMap[name] = configFile
        }
    }

    @Internal
    class ConfigFile(val configuration: Configuration, val file: File) {

        val nodes = CopyOnWriteArraySet<ClassField>()
    }
}