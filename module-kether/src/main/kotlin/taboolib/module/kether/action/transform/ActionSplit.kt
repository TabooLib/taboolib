package taboolib.module.kether.action.transform

import io.izzel.kether.common.api.ParsedAction
import io.izzel.kether.common.api.QuestAction
import io.izzel.kether.common.api.QuestContext
import io.izzel.kether.common.loader.types.ArgTypes
import taboolib.module.kether.Kether.expects
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptParser
import java.util.concurrent.CompletableFuture

/**
 * Zaphkiel
 * ink.ptms.zaphkiel.module.kether.ActionSplit
 *
 * @author sky
 * @since 2021/3/16 2:56 下午
 */
class ActionSplit(val value: ParsedAction<*>, val split: String) : QuestAction<List<String>>() {

    override fun process(frame: QuestContext.Frame): CompletableFuture<List<String>> {
        return frame.newFrame(value).run<Any>().thenApply {
            it.toString().split(split.toRegex())
        }
    }

    override fun toString(): String {
        return "ActionSplit(value=$value, split='$split')"
    }

    companion object {

        /**
         * split *"1 2 3" by " "
         */
        @KetherParser(["split"])
        fun parser() = ScriptParser.parser {
            ActionSplit(
                it.next(ArgTypes.ACTION), try {
                    it.mark()
                    it.expects("by", "with")
                    it.nextToken()
                } catch (ignored: Exception) {
                    it.reset()
                    ""
                }
            )
        }
    }
}