package taboolib.module.kether.action.game

import io.izzel.kether.common.api.ParsedAction
import io.izzel.kether.common.api.QuestAction
import io.izzel.kether.common.api.QuestContext
import io.izzel.kether.common.loader.types.ArgTypes
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptContext
import taboolib.module.kether.ScriptParser
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionPermission(val permission: ParsedAction<*>) : QuestAction<Boolean>() {

    override fun process(context: QuestContext.Frame): CompletableFuture<Boolean> {
        return context.newFrame(permission).run<Any>().thenApply {
            (context.context() as ScriptContext).sender!!.hasPermission(it.toString())
        }
    }

    override fun toString(): String {
        return "ActionPermission(permission='$permission')"
    }

    companion object {

        @KetherParser(["perm", "permission"])
        fun parser() = ScriptParser.parser {
            ActionPermission(it.next(ArgTypes.ACTION))
        }
    }
}