package taboolib.module.nms

object AsmClassLoader : ClassLoader() {

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
        return AsmClassLoader.defineClass(name.replace('/', '.'), arr, 0, arr.size, AsmClassLoader::class.java.protectionDomain)
    }
}