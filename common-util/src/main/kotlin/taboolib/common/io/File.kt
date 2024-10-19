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

/**
 * 深度遍历文件或目录。
 *
 * @param directionMatcher 用于确定是否继续遍历子目录的函数。
 * @return 返回符合条件的所有文件列表。
 */
fun File.deep(directionMatcher: (File) -> Boolean): List<File> {
    val list = arrayListOf<File>()
    if (isDirectory && directionMatcher(this)) {
        list += listFiles()?.flatMap { it.deep(directionMatcher) } ?: emptyList()
    } else {
        list += this
    }
    return list
}