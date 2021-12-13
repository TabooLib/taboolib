@file:Isolated
package taboolib.common.io

import taboolib.common.Isolated
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.Future

private val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())!!

/**
 * Delete the directory and all its contents asynchronously.<br/>
 * if you need to wait for the deletion to complete, pass in a set here and use Set#forEach(Future<*>::get)
 * @param await whether to wait for the deletion to complete
 * @param futures the future set to store future in
 *
 * @author Kylepoops
 */
fun File.deepDeleteAsync(await: Boolean = false, futures: MutableSet<Future<*>>? = null) {
    // first submit the task and get the future
    val future = executor.submit {
        if (this.exists()) {
            if (this.isDirectory) {
                listFiles()?.let { files ->
                    // Construct another future set here
                    // Because we need all the subdirectories and files to be deleted before this directory is deleted
                    val thisFutures = mutableSetOf<Future<*>>()
                    // Pass the future set to this, so we can store all the future
                    files.forEach { it.deepDeleteAsync(futures = thisFutures) }
                    // Wait for all the subdirectories and files to be deleted
                    thisFutures.forEach(Future<*>::get)
                }
            }
            // Finally, delete this file or directory
            this.delete()
        }
    }
    // Add the future to the future set
    futures?.add(future)
    // Wait the task to finish before returning if await is true
    // It shouldn't be called inside this function
    if (await) future.get()
}

/**
 * 删除特定文件夹下的所有子文件
 */
fun File.deepDelete() {
    if (exists()) {
        if (isDirectory) {
            listFiles()?.forEach { it.deepDelete() }
        }
        delete()
    }
}

/**
 * 复制文件或文件夹
 * 若目标为文件夹则复制其所有子文件
 */
fun File.deepCopyTo(target: File) {
    if (isDirectory) {
        listFiles()?.forEach { it.deepCopyTo(File(target, it.name)) }
    } else {
        copyTo(target)
    }
}
