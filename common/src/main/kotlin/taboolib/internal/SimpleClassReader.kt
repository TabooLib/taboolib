package taboolib.internal

import taboolib.common.TabooLib
import taboolib.common.io.ClassReader
import java.io.File
import java.net.JarURLConnection
import java.net.URISyntaxException
import java.net.URL
import java.util.concurrent.CopyOnWriteArrayList
import java.util.jar.JarFile

open class SimpleClassReader : ClassReader {

    override fun readClasses(url: URL): List<Class<*>> {
        val classes = CopyOnWriteArrayList<Class<*>>()
        val src = ArrayList<File>()
        val srcFile = try {
            File(url.toURI())
        } catch (ex: IllegalArgumentException) {
            File((url.openConnection() as JarURLConnection).jarFileURL.toURI())
        } catch (ex: URISyntaxException) {
            File(url.path)
        }
        val springBootWar = srcFile.parentFile.name == "lib" && srcFile.parentFile.parentFile.name == "WEB-INF"
        if (springBootWar) {
            // include taboolib modules
            srcFile.parentFile.listFiles()?.forEach {
                if (it.name.startsWith("taboolib")) {
                    src += it
                }
            }
            fun include(file: File, path: String = "") {
                if (file.isDirectory) {
                    file.listFiles()?.forEach { sub -> include(sub, "$path${file.name}.") }
                } else {
                    kotlin.runCatching {
                        classes.add(Class.forName("$path${file.nameWithoutExtension}"))
                    }
                }
            }
            File(srcFile.parentFile.parentFile, "classes").listFiles()?.forEach {
                include(it)
            }
        } else {
            src += srcFile
        }
        src.forEach { s ->
            JarFile(s).stream().parallel().filter { it.name.endsWith(".class") }.forEach {
                kotlin.runCatching {
                    val forName = Class.forName(it.name.replace('/', '.').substringBeforeLast('.'), false, TabooLib::class.java.classLoader)
                    forName.name // 尝试获取类名来过滤一些无法访问的类
                    classes.add(forName)
                }
            }
        }
        return classes
    }
}