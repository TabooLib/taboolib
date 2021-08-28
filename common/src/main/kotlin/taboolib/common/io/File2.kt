@file:Isolated
package taboolib.common.io

import taboolib.common.Isolated
import java.io.File

fun File.deepDelete() {
    if (exists()) {
        if (isDirectory) {
            listFiles()?.forEach { it.deepDelete() }
        }
        delete()
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