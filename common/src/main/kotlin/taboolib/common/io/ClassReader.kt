package taboolib.common.io

import taboolib.common.boot.SimpleServiceLoader
import java.net.URL

/**
 * @author 坏黑
 * @since 2022/1/28 4:50 PM
 */
interface ClassReader {

    fun readClasses(url: URL): List<Class<*>>

    companion object {

        @JvmField
        val INSTANCE: ClassReader = SimpleServiceLoader.load(ClassReader::class.java)
    }
}