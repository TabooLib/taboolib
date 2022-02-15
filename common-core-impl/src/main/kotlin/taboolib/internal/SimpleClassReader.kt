package taboolib.internal

import taboolib.common.TabooLib
import taboolib.common.io.ClassReader
import taboolib.common.io.groupId
import taboolib.common.io.taboolibId
import java.io.File
import java.net.JarURLConnection
import java.net.URISyntaxException
import java.net.URL
import java.util.concurrent.CopyOnWriteArrayList
import java.util.jar.JarFile

@Internal
class SimpleClassReader : ClassReader {

    override fun readClasses(url: URL): List<Class<*>> {
        val srcFile = try {
            File(url.toURI())
        } catch (_: IllegalArgumentException) {
            File((url.openConnection() as JarURLConnection).jarFileURL.toURI())
        } catch (_: URISyntaxException) {
            File(url.path)
        }

        return readClassFile(srcFile)
    }

    private fun readClassFile(source: File): List<Class<*>> {
        val classes = CopyOnWriteArrayList<Class<*>>()
        val sourceFiles = mutableListOf<File>()
        val isSpringBootWar = source.parentFile.name == "lib" && source.parentFile.parentFile.name == "WEB-INF"

        if (isSpringBootWar) {
            source
                .parentFile.listFiles()
                ?.parallelStream()
                ?.filter { it.name.startsWith(taboolibId) }
                ?.forEach { sourceFiles += it }

            File(source.parentFile.parentFile, "classes")
                .flattenedToList()
                .parallelStream()
                .map(::readToClass)
                .forEach { classes.add(it) }
        } else {
            sourceFiles += source
        }

        sourceFiles.parallelStream()
            .map(::JarFile)
            .filter { it.name.endsWith(".class") }
            .map { it.name.replace('/', '.').substringBeforeLast('.') }
            .filter { it.startsWith(groupId ?: taboolibId) }
            .map { runCatching { Class.forName(it, false, TabooLib::class.java.classLoader) } }
            .forEach { result -> result.getOrNull()?.let { classes.add(it) } }

        return classes
    }

    private fun File.flattenedToList(): List<File> {
        val files = mutableListOf<File>()

        if (isDirectory)
            listFiles()?.toList()
                ?.parallelStream()
                ?.forEach { files += it.flattenedToList() }
        else
            files += this

        return files
    }

    @Throws(IllegalArgumentException::class)
    private fun readToClass(file: File): Class<*> {
        val loader = this::class.java.classLoader
        return try {
            loader.loadClass("${file.path}.${file.nameWithoutExtension}")
        } catch (ex: ClassNotFoundException) {
            throw IllegalArgumentException("file at location ${file.path} is not a valid class file", ex)
        }
    }
}
