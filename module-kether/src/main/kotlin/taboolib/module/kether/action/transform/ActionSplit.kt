package taboolib.module.kether.action.transform

import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.*
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