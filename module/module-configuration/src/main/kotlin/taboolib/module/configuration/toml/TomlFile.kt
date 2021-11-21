package taboolib.module.configuration.toml

import com.moandjiezana.toml.TomlWriter
import taboolib.common.platform.function.warning
import taboolib.library.configuration.InvalidConfigurationException
import taboolib.module.configuration.Configuration
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.io.Reader
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.function.Consumer


/**
 * TabooLib
 * taboolib.module.configuration.toml.TomlFileSection
 *
 * @author mac
 * @since 2021/11/22 12:49 上午
 */
class TomlFile : TomlSection(), Configuration {

    override var file: File? = null

    private val hook = ArrayList<Runnable>()

    override fun onReload(runnable: Runnable) {
        hook.add(runnable)
    }

    override fun saveToString(): String {
        synchronized(lock) {
            val byteArrayOutputStream = ByteArrayOutputStream()
            val tomlWriter = TomlWriter()
            tomlWriter.write(root.toMap(), byteArrayOutputStream)
            return byteArrayOutputStream.toByteArray().decodeToString()
        }
    }

    override fun saveToFile(file: File?) {
        synchronized(lock) {
            val byteArrayOutputStream = ByteArrayOutputStream()
            val tomlWriter = TomlWriter()
            tomlWriter.write(root.toMap(), byteArrayOutputStream)
            (file ?: this.file)?.writeBytes(byteArrayOutputStream.toByteArray())
        }
    }

    override fun loadFromFile(file: File) {
        this.file = file
        val content = file.readText(StandardCharsets.UTF_8)
        try {
            root.read(content)
        } catch (ex: InvalidConfigurationException) {
            if (!file.name.endsWith(".bak")) {
                file.copyTo(File(file.parent, file.name + "_" + SimpleDateFormat("yyyyMMddHHmmss").format(System.currentTimeMillis()) + ".bak"))
            }
            throw ex
        }
        hook.forEach { it.run() }
    }

    override fun loadFromString(contents: String) {
        try {
            root.read(contents)
        } catch (t: InvalidConfigurationException) {
            warning("Source: \n$contents")
            throw t
        }
        hook.forEach { it.run() }
    }

    override fun loadFromReader(reader: Reader) {
        root.read(reader)
        hook.forEach { it.run() }
    }

    override fun loadFromInputStream(inputStream: InputStream) {
        root.read(inputStream)
        hook.forEach { it.run() }
    }

    override fun reload() {
        loadFromFile(file ?: return)
    }
}