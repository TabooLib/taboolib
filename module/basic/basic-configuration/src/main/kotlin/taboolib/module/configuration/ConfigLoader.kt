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
import java.nio.charset.Charset

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
            val rawName = configAnno.property("value", "config.yml")
            val name = fixChineseFilename(rawName)
            val rawTarget = configAnno.property("target", name)
            val target = fixChineseFilename(rawTarget).let {
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
        
        /**
         * 修复中文文件名编码问题
         * 当注解处理时中文字符被损坏（如："测试"变成"测试？"），尝试修复编码
         */
        private fun fixChineseFilename(filename: String): String {
            // 如果文件名包含问号，可能是中文字符编码损坏
            if (filename.contains('?')) {
                try {
                    // 尝试查找已有的正确文件名
                    val correctFilename = files.keys.find { key ->
                        // 尝试匹配损坏前的长度和前缀
                        key.length >= filename.replace("?", "").length && 
                        key.startsWith(filename.substringBefore('?'))
                    }
                    if (correctFilename != null) {
                        PrimitiveIO.debug("找到正确的中文文件名: '$filename' -> '$correctFilename'")
                        return correctFilename
                    }
                    
                    // 如果没找到匹配的文件名，尝试编码修复
                    val bytes = filename.toByteArray(Charsets.ISO_8859_1)
                    val fixedName = String(bytes, Charsets.UTF_8)
                    if (fixedName != filename) {
                        PrimitiveIO.debug("文件名编码修复: '$filename' -> '$fixedName'")
                        return fixedName
                    }
                } catch (e: Exception) {
                    PrimitiveIO.debug("文件名编码修复失败: $filename - ${e.message}")
                }
            }
            return filename
        }
    }
}