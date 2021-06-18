package taboolib.module.nms

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.ClassRemapper

/**
 * TabooLib
 * taboolib.module.nms.ClassTransfer
 *
 * @author sky
 * @since 2021/6/18 1:49 上午
 */
class AsmClassTransfer(val source: String) {

    fun run(): Class<*> {
        val classReader = ClassReader(source)
        val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS)
        val classVisitor: ClassVisitor = ClassRemapper(classWriter, MinecraftRemapper(remapper))
        classReader.accept(classVisitor, 0)
        return AsmClassLoader.createNewClass(source, classWriter.toByteArray())
    }

    companion object {

        val remapper = HashMap<String, String>()

        init {
            val nms = "net/minecraft/server/${MinecraftVersion.legacyVersion}/"
            val mapping = MinecraftVersion.mapping
            if (mapping != null) {
                mapping.fields.forEach {
                    if (MinecraftVersion.isUniversal) {
                        remapper["$nms${it.className}.${it.translateName}"] = "${it.path}.${it.mojangName}"
                    }
                }
                mapping.classMap.forEach { (k, v) ->
                    if (MinecraftVersion.isUniversal) {
                        remapper["$nms$k"] = v
                    } else {
                        remapper[v] = "$nms$k"
                    }
                }
            }
        }
    }
}