package taboolib.module.configuration

import com.electronwill.nightconfig.core.Config
import com.electronwill.nightconfig.core.file.FileNotFoundAction
import com.electronwill.nightconfig.core.io.ConfigParser
import com.electronwill.nightconfig.core.io.ParsingMode
import org.tabooproject.reflex.Reflex.Companion.setProperty
import taboolib.common.platform.function.warning
import java.io.File
import java.io.InputStream
import java.io.Reader
import java.text.SimpleDateFormat

/**
 * TabooLib
 * taboolib.module.configuration.ConfigFile
 *
 * @author mac
 * @since 2021/11/22 12:49 上午
 */
/**
 * 表示一个配置文件，继承自 ConfigSection 并实现 Configuration 接口。
 *
 * @property file 与此配置关联的文件对象，可为 null
 * @property name 配置文件的名称（不包含扩展名）
 *
 * @param root 根配置对象
 */
open class ConfigFile(root: Config) : ConfigSection(root), Configuration {

    override var file: File? = null
    override var name: String = ""

    // 存储重载回调的列表
    private val reloadCallback = ArrayList<Runnable>()

    /**
     * 添加一个在配置重载时执行的回调。
     *
     * @param runnable 要执行的回调
     */
    override fun onReload(runnable: Runnable) {
        reloadCallback.add(runnable)
    }

    /**
     * 将配置保存为字符串。
     *
     * @return 表示配置内容的字符串
     */
    override fun saveToString(): String {
        return toString()
    }

    /**
     * 将配置保存到指定文件。
     *
     * @param file 要保存到的文件，如果为 null 则使用当前关联的文件
     * @throws IllegalStateException 如果未指定文件
     */
    override fun saveToFile(file: File?) {
        (file ?: this.file)?.writeText(saveToString()) ?: error("File not specified")
    }

    /**
     * 从指定文件加载配置。
     *
     * @param file 要加载的文件
     * @throws Exception 如果加载过程中发生错误
     */
    override fun loadFromFile(file: File) {
        this.file = file
        this.name = file.nameWithoutExtension
        try {
            clear()
            parser().parse(file, root, ParsingMode.REPLACE, FileNotFoundAction.THROW_ERROR)
        } catch (ex: Exception) {
            // 如果加载失败且文件扩展名不是 .bak，则创建备份
            if (file.extension != "bak") {
                file.copyTo(File(file.parent, file.name + "_" + SimpleDateFormat("yyyyMMddHHmmss").format(System.currentTimeMillis()) + ".bak"))
            }
            warning("File: $file")
            throw ex
        }
        reloadCallback.forEach { it.run() }
    }

    /**
     * 从字符串加载配置。
     *
     * @param contents 包含配置内容的字符串
     * @throws Exception 如果解析过程中发生错误
     */
    override fun loadFromString(contents: String) {
        try {
            clear()
            parser().parse(contents, root, ParsingMode.REPLACE)
        } catch (t: Exception) {
            warning("Source: \n$contents")
            throw t
        }
        reloadCallback.forEach { it.run() }
    }

    /**
     * 从 Reader 加载配置。
     *
     * @param reader 用于读取配置的 Reader 对象
     */
    override fun loadFromReader(reader: Reader) {
        clear()
        parser().parse(reader, root, ParsingMode.REPLACE)
        reloadCallback.forEach { it.run() }
    }

    /**
     * 从 InputStream 加载配置。
     *
     * @param inputStream 用于读取配置的 InputStream 对象
     */
    override fun loadFromInputStream(inputStream: InputStream) {
        clear()
        parser().parse(inputStream, root, ParsingMode.REPLACE)
        reloadCallback.forEach { it.run() }
    }

    /**
     * 重新加载配置。
     */
    override fun reload() {
        loadFromFile(file ?: return)
    }

    /**
     * 更改配置的类型。
     *
     * @param type 新的配置类型
     */
    override fun changeType(type: Type) {
        val format = type.newFormat()
        fun process(value: Any) {
            when (value) {
                is Map<*, *> -> value.forEach { process(it.value ?: return@forEach) }
                is List<*> -> value.forEach { process(it ?: return@forEach) }
                is Config -> {
                    value.setProperty("configFormat", format)
                    value.valueMap().forEach { process(it.value ?: return) }
                }
            }
        }
        process(root)
    }

    /**
     * 获取与当前配置格式相关联的解析器。
     *
     * @return 配置解析器
     */
    private fun parser(): ConfigParser<out Config> {
        return root.configFormat().createParser()
    }
}