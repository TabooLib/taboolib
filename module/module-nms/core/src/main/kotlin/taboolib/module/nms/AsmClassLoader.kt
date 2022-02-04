package taboolib.module.nms

import taboolib.common.TabooLib

object AsmClassLoader : ClassLoader(TabooLib::class.java.classLoader) {

    override fun findClass(name: String?): Class<*> {
        try {
            return Class.forName(name, false, AsmClassLoader::class.java.classLoader)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return super.findClass(name)
    }

    @JvmStatic
    fun createNewClass(name: String, arr: ByteArray): Class<*> {
        return defineClass(name.replace('/', '.'), arr, 0, arr.size, AsmClassLoader::class.java.protectionDomain)
    }
}