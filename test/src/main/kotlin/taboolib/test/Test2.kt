package taboolib.test

import taboolib.common.TabooLibCommon
import taboolib.library.configuration.MemorySection
import taboolib.module.configuration.ConfigLoader
import taboolib.module.configuration.SecuredFile
import taboolib.module.configuration.contrastAs

object Test2 {

    @JvmStatic
    fun main(args: Array<String>) {
        TabooLibCommon.ENV.inject(ConfigLoader::class.java)

        val map1 = HashMap<String, Any>()
        map1["a"] = 1
        map1["b"] = 2
        map1["c"] = hashMapOf("1" to "b", "2" to "c")
        map1["d"] = hashMapOf("1" to "b", "2" to "c")

        val map2 = HashMap<String, Any>()
        map2["a"] = 1
        map2["b"] = 1
        map2["d"] = hashMapOf("1" to "3")

        val conf = SecuredFile()
        conf.set("a.b", "c")
        conf.set("d", "e")

        map1.contrastAs(map2).forEach {
            println(it)
        }
    }
}