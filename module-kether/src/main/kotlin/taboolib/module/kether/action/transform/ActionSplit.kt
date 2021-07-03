package taboolib.module.kether.action.transform

import io.izzel.kether.common.api.ParsedAction
import io.izzel.kether.common.loader.types.ArgTypes
import taboolib.module.kether.Kether.expects
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.scriptParser
import java.util.concurrent.CompletableFuture

/**
 * Zaphkiel
 * ink.ptms.zaphkiel.module.kether.ActionSplit
 *
 * @author sky
 * @since 2021/3/16 2:56 下午
 */
class ActionSplit(val value: ParsedAction<*>, val split: String) : ScriptAction<List<String>>() {

    override fun run(frame: ScriptFrame): CompletableFuture<List<String>> {
        return frame.newFrame(value).run<Any>().thenApply {
            it.toString().split(split.toRegex())
        }
    }

    internal object Parser {

        /**
         * split *"1 2 3" by " "
         */
        @KetherParser(["split"])
        fun parser() = scriptParser {
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