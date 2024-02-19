package taboolib.module.nms

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.ClassRemapper
import taboolib.common.TabooLib

/**
 * TabooLib
 * taboolib.module.nms.ClassTransfer
 *
 * @author sky
 * @since 2021/6/18 1:49 上午
 */
class AsmClassTransfer(val source: String) {

    @Synchronized
    fun createNewClass(): Class<*> {
        var inputStream = AsmClassTransfer::class.java.classLoader.getResourceAsStream(source.replace('.', '/') + ".class")
        if (inputStream == null) {
            inputStream = TabooLib::class.java.classLoader.getResourceAsStream(source.replace('.', '/') + ".class")
        }
        if (inputStream == null) {
            error("Cannot find class: $source")
        }
        val classReader = ClassReader(inputStream)
        val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS)
        val classVisitor: ClassVisitor = ClassRemapper(classWriter, MinecraftRemapper())
        classReader.accept(classVisitor, 0)
        return AsmClassLoader.createNewClass(source, classWriter.toByteArray())
    }
}