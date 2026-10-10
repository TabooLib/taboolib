package taboolib.common.io

import java.io.File
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

/**
 * Delete the directory and all its contents asynchronously.<br/>
 * if you need to wait for the deletion to complete, pass in a set here and use Set#forEach(Future<*>::get)
 * @param await whether to wait for the deletion to complete
 * @param futures the future set to store future in
 *
 * @author Kylepoops
 */
fun File.deepDeleteAsync(await: Boolean = false, futures: MutableSet<Future<*>>? = null) {
    // Traverse the whole tree in one asynchronous task. Submitting child tasks and waiting for them
    // from the same bounded executor can exhaust every worker and deadlock on sufficiently deep trees.
    val future = CompletableFuture.runAsync { deleteTree(toPath()) }
    futures?.add(future)
    if (await) {
        future.get()
    }
}

private fun deleteTree(root: Path) {
    if (!Files.exists(root, LinkOption.NOFOLLOW_LINKS)) {
        return
    }
    Files.walkFileTree(root, object : SimpleFileVisitor<Path>() {

        override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
            Files.deleteIfExists(file)
            return FileVisitResult.CONTINUE
        }

        override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
            if (exc != null) {
                throw exc
            }
            Files.deleteIfExists(dir)
            return FileVisitResult.CONTINUE
        }
    })
}
