package taboolib.test

import taboolib.common.TabooLibCommon
import taboolib.library.configuration.MemorySection
import taboolib.module.configuration.*
import java.nio.charset.StandardCharsets

object Test2 {

    @JvmStatic
    fun main(args: Array<String>) {
        TabooLibCommon.ENV.inject(ConfigLoader::class.java)

        val f1 = SecuredFile().also {
            it.set("a", 1)
            it.set("b", 2)
            it.set("c", hashMapOf("1" to "a", "2" to "b", "3" to hashMapOf("1" to "a", "2" to "b")))
            it.set("d", hashMapOf("1" to "a", "2" to "b"))
        }

        val f2 = SecuredFile().also {
            it.set("b", 1)
            it.set("c", hashMapOf("2" to "b"))
            it.set("d", hashMapOf("1" to "changed"))
        }

        val f3 = """
a: 1
b: 2
# 注释1
c:
  '1': a
  '2': b
  # 注释2
  '3':
    # 注释3
    '1': a
    # 注释4
    # 注释5
    '2': b
d:
  '1': a
  # 注释6
  '2': b
        """.trimIndent()
        f3.byteInputStream().migrateTo(f2.saveToString().byteInputStream())

//        println(f1.saveToString())
//        println(ConfigFinder.findNode("settings.d.1", f1.saveToString()))
//        println(ConfigFinder.findNode("a.b.c", """
//            c: 0
//            a:
//              # 456
//              b: 0
//                # 123
//
//                # 哦耶
//                # 你妈死了
//                c: 0
//            b: 0
//        """.trimIndent()))
    }
}