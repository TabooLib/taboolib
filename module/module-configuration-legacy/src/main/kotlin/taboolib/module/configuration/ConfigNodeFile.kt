package taboolib.module.configuration

import org.tabooproject.reflex.ClassField
import java.io.File
import java.util.concurrent.CopyOnWriteArraySet

class ConfigNodeFile(val conf: SecuredFile, val file: File) {

    val nodes = CopyOnWriteArraySet<ClassField>()
}