package taboolib.module.configuration

import org.tabooproject.reflex.ClassField
import taboolib.common.LifeCycle
import taboolib.common.PrimitiveIO
import taboolib.common.PrimitiveSettings
import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency
import taboolib.common.inject.ClassVisitor
import taboolib.common.platform.Awake
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.function.releaseResourceFile
import taboolib.common.util.unsafeLazy
import taboolib.common5.FileWatcher
import java.util.function.Supplier

@RuntimeDependencies(
    RuntimeDependency(
        "!org.yaml:snakeyaml:2.2",
        test = "!org.yaml.snakeyaml_2_2.Yaml",
        relocate = ["!org.yaml.snakeyaml", "!org.yaml.snakeyaml_2_2"]
    ),
    RuntimeDependency(
        "!com.typesafe:config:1.4.3",
        test = "!com.typesafe.config_1_4_3.Config",
        relocate = ["!com.typesafe.config", "!com.typesafe.config_1_4_3"]
    ),
    RuntimeDependency(
        "!com.electronwill.night-config:core:3.6.7",
        test = "!com.electronwill.nightconfig_3_6_7.core.Config",
        relocate = ["!com.electronwill.nightconfig", "!com.electronwill.nightconfig_3_6_7", "!com.typesafe.config", "!com.typesafe.config_1_4_3"]
    ),
    RuntimeDependency(
        "!com.electronwill.night-config:toml:3.6.7",
        test = "!com.electronwill.nightconfig_3_6_7.toml.TomlFormat",
        relocate = ["!com.electronwill.nightconfig", "!com.electronwill.nightconfig_3_6_7", "!com.typesafe.config", "!com.typesafe.config_1_4_3"]
    ),
    RuntimeDependency(
        "!com.electronwill.night-config:json:3.6.7",
        test = "!com.electronwill.nightconfig_3_6_7.json.JsonFormat",
        relocate = ["!com.electronwill.nightconfig", "!com.electronwill.nightconfig_3_6_7", "!com.typesafe.config", "!com.typesafe.config_1_4_3"]
    ),
    RuntimeDependency(
        "!com.electronwill.night-config:hocon:3.6.7",
        test = "!com.electronwill.nightconfig_3_6_7.hocon.HoconFormat",
        relocate = ["!com.electronwill.nightconfig", "!com.electronwill.nightconfig_3_6_7", "!com.typesafe.config", "!com.typesafe.config_1_4_3"]
    )
)
@Awake
class ConfigLoader : ClassVisitor(1) {

    @Suppress("DEPRECATION")
    override fun visit(field: ClassField, clazz: Class<*>, instance: Supplier<*>?) {
        if (field.isAnnotationPresent(Config::class.java)) {
            val configAnno = field.getAnnotation(Config::class.java)
            val name = configAnno.property("value", "config.yml")
            val target = configAnno.property("target", name).let {
                it.ifEmpty { name }
            }
            if (files.containsKey(name)) {
                field.set(instance?.get(), files[name]!!.configuration)
            } else {
                val file = releaseResourceFile(name, target = target)
                // 兼容模式加载
                val conf = if (field.fieldType == SecuredFile::class.java) {
                    SecuredFile.loadConfiguration(file)
                } else {
                    Configuration.loadFromFile(file, concurrent = configAnno.property("concurrent", true))
                }
                // 赋值
                field.set(instance?.get(), conf)
                // 自动重载
                if (configAnno.property("autoReload", false) && isFileWatcherHook) {
                    FileWatcher.INSTANCE.addSimpleListener(file) {
                        if (file.exists()) {
                            conf.loadFromFile(file)
                        }
                    }
                }
                val configFile = ConfigNodeFile(conf, file)
                conf.onReload {
                    val loader = PlatformFactory.getAPI<ConfigNodeLoader>()
                    configFile.nodes.forEach { loader.visit(it, clazz, instance) }
                }
                files[name] = configFile
                // 开发模式
                if (PrimitiveSettings.IS_DEBUG_MODE) {
                    PrimitiveIO.println("Loaded config file: ${file.absolutePath}")
                }
            }
        }
    }

    override fun getLifeCycle(): LifeCycle {
        return LifeCycle.INIT
    }

    companion object {

        val files = HashMap<String, ConfigNodeFile>()

        val isFileWatcherHook by unsafeLazy {
            try {
                FileWatcher.INSTANCE
                true
            } catch (ex: NoClassDefFoundError) {
                false
            }
        }
    }
}