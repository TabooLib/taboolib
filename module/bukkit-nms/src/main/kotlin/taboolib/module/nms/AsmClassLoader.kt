package taboolib.module.nms

import taboolib.common.ClassAppender
import taboolib.common.io.isDebugMode

object AsmClassLoader : ClassLoader(ClassAppender.getClassLoader()) {

    override fun findClass(name: String?): Class<*> {
        try {
            return Class.forName(name, false, AsmClassLoader::class.java.classLoader)
        } catch (e: Throwable) {
            if (isDebugMode) e.printStackTrace()
        }
        return super.findClass(name)
    }

    @JvmStatic
    fun createNewClass(name: String, arr: ByteArray): Class<*> {
        return defineClass(name.replace('/', '.'), arr, 0, arr.size, AsmClassLoader::class.java.protectionDomain)
    }
}