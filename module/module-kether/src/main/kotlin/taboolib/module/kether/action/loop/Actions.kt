package taboolib.module.kether.action.loop

import taboolib.module.kether.KetherParser
import taboolib.module.kether.actionNow
import taboolib.module.kether.script
import taboolib.module.kether.scriptParser

object Actions {

    @KetherParser(["break"])
    fun actionBreak() = scriptParser {
        actionNow {
            script().breakLoop = true
            null
        }
    }
}