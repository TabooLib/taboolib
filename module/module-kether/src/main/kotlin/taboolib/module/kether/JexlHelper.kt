package taboolib.module.kether

import org.apache.commons.jexl3.MapContext
import taboolib.module.kether.action.transform.ActionJexl3

/**
 * 将字符串编译为 JexlScript
 */
fun String.compileToJexl(): JexlScript {
    val script = ActionJexl3.jexl.createScript(this)
    return object : JexlScript {

        override fun eval(map: Map<String, Any?>): Any? {
            return script.execute(MapContext())
        }
    }
}

interface JexlScript {

    /** 执行脚本 */
    fun eval(map: Map<String, Any?> = emptyMap()): Any?
}