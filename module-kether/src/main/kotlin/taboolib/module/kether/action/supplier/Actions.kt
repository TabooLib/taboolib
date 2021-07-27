package taboolib.module.kether.action.supplier

import taboolib.module.kether.KetherParser
import taboolib.module.kether.actionNow
import taboolib.module.kether.deepVars
import taboolib.module.kether.scriptParser
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
}