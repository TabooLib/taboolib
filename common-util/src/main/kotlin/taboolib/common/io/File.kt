package taboolib.common.io

import java.io.File

fun File.notfound(): Boolean {
    return !exists()
}

/**
 * 复制文件或文件夹
 * 若目标为文件夹则复制其所有子文件
 * @return 返回自己
 */
fun File.deepCopyTo(target: File): File {
    if (isDirectory) {
        listFiles()?.forEach { it.deepCopyTo(File(target, it.name)) }
    } else {
        copyTo(target, overwrite = true)
    }
    return this
}
