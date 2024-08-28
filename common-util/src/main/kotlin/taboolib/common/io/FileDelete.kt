package taboolib.common.io

import java.io.File

/**
 * 删除特定文件夹下的所有子文件
 * @return 返回自己
 */
fun File.deepDelete(): File {
    if (exists()) {
        if (isDirectory) {
            listFiles()?.forEach { it.deepDelete() }
        }
        delete()
    }
    return this
}