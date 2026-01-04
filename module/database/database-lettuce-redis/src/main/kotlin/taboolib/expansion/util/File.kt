package taboolib.expansion.util

import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.releaseResourceFile
import java.io.File
import kotlin.collections.forEach

internal inline fun <T> files(path: String, vararg defs: String, callback: (File) -> T): List<T> {
    val file = File(getDataFolder(), path)
    if (!file.exists()) {
        defs.forEach {
            releaseResourceFile("$path/$it", false)
        }
    }
    return getFiles(file).map { callback(it) }
}

internal fun getFiles(file: File): List<File> {
    val listOf = mutableListOf<File>()
    when (file.isDirectory) {
        true -> file.listFiles()?.let { files ->
            listOf += files.flatMap { getFiles(it) }
        }
        false -> {
            if (file.name.endsWith(".yml")) {
                listOf += file
            }
        }
    }
    return listOf
}