@file:Suppress("UNCHECKED_CAST", "NO_REFLECTION_IN_CLASS_PATH")

package taboolib.common.io

import taboolib.common.TabooLibCommon
import taboolib.common.inject.RuntimeInjector
import taboolib.common.platform.PlatformFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.math.BigInteger
import java.net.URISyntaxException
import java.net.URL
import java.security.MessageDigest
import java.util.jar.JarFile
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

val runningClasses = TabooLibCommon::class.java.protectionDomain.codeSource.location.getClasses()

fun <T> Class<T>.getInstance(new: Boolean = false): T? {
    try {
        val awoken = PlatformFactory.getAPI<T>(simpleName)
        if (awoken != null) {
            return awoken
        }
    } catch (ex: NoClassDefFoundError) {
        return null
    } catch (ex: ClassNotFoundException) {
        return null
    }
    return try {
        getDeclaredField("INSTANCE").get(null) as T
    } catch (ex: ExceptionInInitializerError) {
        println("error: $this")
        ex.printStackTrace()
        null
    } catch (ex: NoClassDefFoundError) {
        null
    } catch (ex: IllegalAccessException) {
        null
    } catch (ex: NoSuchFieldException) {
        if (new) getDeclaredConstructor().newInstance() as T else null
    }
}

fun <T> Class<T>.findInstance(): T? {
    return runningClasses.firstOrNull { isAssignableFrom(it) && this != it }?.getInstance(new = true) as? T
}

fun <T> inject(clazz: Class<T>) {
    return RuntimeInjector.injectAll(clazz)
}

fun URL.getClasses(): List<Class<*>> {
    val src = try {
        File(toURI())
    } catch (ex: URISyntaxException) {
        File(path)
    }
    val classes = ArrayList<Class<*>>()
    JarFile(src).stream().filter { it.name.endsWith(".class") }.forEach {
        try {
            classes.add(Class.forName(it.name.replace('/', '.').substring(0, it.name.length - 6), false, TabooLibCommon::class.java.classLoader))
        } catch (ex: Throwable) {
        }
    }
    return classes
}

fun File.digest(algorithm: String): String {
    return FileInputStream(this).use {
        val digest = MessageDigest.getInstance(algorithm)
        val buffer = ByteArray(1024)
        var length: Int
        while (it.read(buffer, 0, 1024).also { i -> length = i } != -1) {
            digest.update(buffer, 0, length)
        }
        val bytes = digest.digest()
        BigInteger(1, bytes).toString(16)
    }
}

fun File.deepCopyTo(target: File) {
    if (!target.exists()) {
        if (isDirectory) {
            target.mkdirs()
        } else {
            target.createNewFile()
        }
    }
    if (isDirectory) {
        listFiles()?.forEach { it.deepCopyTo(File(target, it.name)) }
    } else {
        copyTo(target)
    }
}

fun File.deepDelete() {
    if (exists()) {
        if (isDirectory) {
            listFiles()?.forEach { it.deepDelete() }
        } else {
            delete()
        }
    }
}

fun File.toZip(target: File) {
    FileOutputStream(target).use { fileOutputStream -> ZipOutputStream(fileOutputStream).use { it -> it.toZip(this, "") } }
}

fun File.toZipSkipDirectory(target: File) {
    FileOutputStream(target).use { fileOutputStream ->
        ZipOutputStream(fileOutputStream).use { zipOutputStream ->
            if (isDirectory) {
                listFiles()?.forEach { zipOutputStream.toZip(it, "") }
            } else {
                zipOutputStream.toZip(this, "")
            }
        }
    }
}

fun ZipOutputStream.toZip(file: File, path: String) {
    if (file.isDirectory) {
        file.listFiles()?.forEach { toZip(it, path + file.name + "/") }
    } else {
        FileInputStream(file).use {
            putNextEntry(ZipEntry(path + file.name))
            write(it.readBytes())
            flush()
            closeEntry()
        }
    }
}

fun File.fromZip(target: File) {
    fromZip(target.path)
}

fun File.fromZip(destDirPath: String) {
    ZipFile(this).use { zipFile ->
        zipFile.stream().forEach { entry ->
            if (entry.isDirectory) {
                File(destDirPath + "/" + entry.name).mkdirs()
            } else {
                zipFile.getInputStream(entry).use {
                    File(destDirPath + "/" + entry.name).writeBytes(it.readBytes())
                }
            }
        }
    }
}