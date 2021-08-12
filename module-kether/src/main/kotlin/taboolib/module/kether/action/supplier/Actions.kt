package taboolib.module.kether.action.supplier

import taboolib.common.OpenResult
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture


/**
 * @author IzzelAliz
 */
internal object Actions {

    @KetherParser(["null"])
    fun parser1() = scriptParser {
        actionNow {
            CompletableFuture.completedFuture(null)
        }
    }

    @KetherParser(["pass"])
    fun parser2() = scriptParser {
        actionNow {
            CompletableFuture.completedFuture("")
        }
    }

    @KetherParser(["vars", "variables"])
    fun parser3() = scriptParser {
        actionNow {
            CompletableFuture.completedFuture(deepVars().keys.toList())
        }
    }

    @KetherProperty(bind = String::class)
    fun property() = object : ScriptProperty("string") {

        override fun read(instance: Any, key: String): OpenResult {
            return when (key) {
                "length", "size" -> OpenResult.successful((instance as String).length)
                else -> OpenResult.failed()
            }
        }

        override fun write(instance: Any, key: String, value: Any?): OpenResult {
            return OpenResult.failed()
        }
    }
}