package taboolib.module.configuration

import org.tabooproject.reflex.ClassField
import org.tabooproject.reflex.ReflexClass
import taboolib.common.Inject
import taboolib.common.LifeCycle
import taboolib.common.PrimitiveIO
import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency
import taboolib.common.inject.ClassVisitor
import taboolib.common.platform.Awake
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.function.releaseResourceFile
import taboolib.common5.FileWatcher

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
@Inject
@Awake
class ConfigLoader : ClassVisitor(1) {

    override fun visit(field: ClassField, owner: ReflexClass) {
        if (field.isAnnotationPresent(Config::class.java)) {
            val configAnno = field.getAnnotation(Config::class.java)
            val name = configAnno.property("value", "config.yml")
            val target = configAnno.property("target", name).let {
                it.ifEmpty { name }
            }
            if (files.containsKey(name)) {
                field.set(findInstance(owner), files[name]!!.configuration)
            } else {
                val file = releaseResourceFile(name, target = target)
                // 兼容模式加载
                val conf = if (field.fieldType == SecuredFile::class.java) {
                    SecuredFile.loadConfiguration(file)
                } else {
                    Configuration.loadFromFile(file, concurrent = configAnno.property("concurrent", true))
                }
                // 迁移
                if (configAnno.property("migrate", false)) {
                    migrateConfig(name, conf)
                }
                // 赋值
                field.set(findInstance(owner), conf)
                // 自动重载
                if (configAnno.property("autoReload", false)) {
                    PrimitiveIO.debug("正在监听文件变更: ${file.absolutePath}")
                    FileWatcher.INSTANCE.addSimpleListener(file) {
                        PrimitiveIO.debug("文件变更: ${file.absolutePath}")
                        if (file.exists()) {
                            conf.loadFromFile(file)
                        }
                    }
                }
                val configFile = ConfigNodeFile(conf, file)
                conf.onReload {
                    val loader = PlatformFactory.getAPI<ConfigNodeLoader>(ConfigNodeLoader::class.java.name)
                    configFile.nodes.forEach { loader.visit(it, owner) }
                }
                files[name] = configFile
                // 开发模式
                PrimitiveIO.debug("加载配置文件: ${file.absolutePath}")
            }
        }
    }

    override fun getLifeCycle(): LifeCycle {
        return LifeCycle.INIT
    }

    companion object {

        val files = HashMap<String, ConfigNodeFile>()

        private fun migrateConfig(resourceName: String, conf: Configuration) {
            val inputStream = ConfigLoader::class.java.classLoader.getResourceAsStream(resourceName) ?: return
            val source = Configuration.loadFromInputStream(inputStream, conf.type)
            val sourceKeys = source.getKeys(true)
            val targetKeys = conf.getKeys(true)
            val missingKeys = sourceKeys - targetKeys
            if (missingKeys.isEmpty()) return
            // 收集需要迁移注释的 section 路径
            val sectionPaths = LinkedHashSet<String>()
            missingKeys.forEach { key ->
                // 迁移值
                conf[key] = source[key]
                // 迁移叶子节点注释
                source.getComment(key)?.let { conf.setComment(key, it) }
                // 收集父级 section 路径
                var path = key
                while (path.contains('.')) {
                    path = path.substringBeforeLast('.')
                    sectionPaths += path
                }
            }
            // 迁移 section 注释（仅当目标配置中该 section 之前不存在时）
            sectionPaths.forEach { path ->
                if (conf.getComment(path) == null) {
                    source.getComment(path)?.let { conf.setComment(path, it) }
                }
            }
            conf.saveToFile()
            PrimitiveIO.debug("配置文件迁移完成，补全了 ${missingKeys.size} 个缺失节点: $resourceName")
        }
    }
}