package taboolib.test

import taboolib.common.TabooLib
import taboolib.common.io.ClassReader
import java.io.File
import java.net.URL

/**
 * TabooLib
 * taboolib.test.TestClassReader
 *
 * @author 坏黑
 * @since 2022/1/28 5:21 PM
 */
class TestClassReader : ClassReader {

    val files = ArrayList<File>()

    init {
        add(File("build/classes/java/main"))
        add(File("build/classes/kotlin/main"))
        add(File("build/classes/kotlin/test"))
    }

    fun add(file: File) {
        files += file
    }

    override fun readClasses(url: URL): List<Class<*>> {
        val classes = ArrayList<Class<*>>()
        fun loadClasses(file: File, parent: File) {
            if (file.isDirectory) {
                file.listFiles()?.forEach { loadClasses(it, parent) }
            } else if (file.extension == "class") {
                val name = file.path.substring(parent.path.length + 1).substringBeforeLast('.').replace('/', '.')
                classes.add(Class.forName(name, false, TabooLib::class.java.classLoader))
            }
        }
        files.forEach { loadClasses(it, it) }
        return classes
    }
}