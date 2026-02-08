import taboolib.common5.FileWatcher
import java.io.File

fun main() {
     FileWatcher.INSTANCE.addSimpleListener(File("test.txt")) {
         println("change")
     }
}