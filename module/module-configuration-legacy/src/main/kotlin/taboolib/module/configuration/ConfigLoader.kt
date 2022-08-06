package taboolib.module.configuration

import org.tabooproject.reflex.ClassField
import org.tabooproject.reflex.ClassMethod
import taboolib.common.LifeCycle
import taboolib.common.env.RuntimeDependency
import taboolib.common.inject.ClassVisitor
import taboolib.common.platform.Awake
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.function.releaseResourceFile
import taboolib.common5.FileWatcher
import java.util.function.Supplier

@RuntimeDependency("!org.yaml:snakeyaml:1.28", test = "!org.yaml.snakeyaml.Yaml")
@Awake
class ConfigLoader : ClassVisitor(1) {

    override fun visit(field: ClassField, clazz: Class<*>, instance: Supplier<*>?) {
        if (field.isAnnotationPresent(Config::class.java)) {
            val configAnno = field.getAnnotation(Config::class.java)
            val name = configAnno.property("value", "config.yml")
            if (files.containsKey(name)) {
                field.set(instance?.get(), files[name]!!.conf)
            } else {
                val file = releaseResourceFile(name)
                if (configAnno.property("migrate", false)) {
                    val resourceAsStream = clazz.classLoader.getResourceAsStream(file.name)
                    if (resourceAsStream != null) {
                        val bytes = resourceAsStream.migrateTo(file.inputStream())
                        if (bytes != null) {
                            file.writeBytes(bytes)
                        }
                    }
                }
                val conf = SecuredFile.loadConfiguration(file)
                field.set(instance?.get(), conf)
                // 自动重载文件
                if (configAnno.property("autoReload", false) && isFileWatcherHook) {
                    FileWatcher.INSTANCE.addSimpleListener(file) {
                        if (file.exists()) {
                            conf.load(file)
                        }
                    }
                }
                val nodeFile = ConfigNodeFile(conf, file)
                // 自动重载节点
                conf.onReload {
                    val loader = PlatformFactory.getAPI<ConfigNodeLoader>()
                    nodeFile.nodes.forEach { loader.visit(it, clazz, instance) }
                }
                files[name] = nodeFile
            }
        }
    }

    override fun getLifeCycle(): LifeCycle {
        return LifeCycle.INIT
    }

    companion object {

        val files = HashMap<String, ConfigNodeFile>()

        val isFileWatcherHook by lazy {
            try {
                FileWatcher.INSTANCE
                true
            } catch (ex: NoClassDefFoundError) {
                false
            }
        }
    }
}