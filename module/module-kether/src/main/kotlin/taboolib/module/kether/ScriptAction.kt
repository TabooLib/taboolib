package taboolib.module.kether

import taboolib.library.kether.QuestAction
import java.util.concurrent.CompletableFuture

/**
 * TabooLib
 * taboolib.module.kether.ScriptAction
 *
 * @author sky
 * @since 2021/7/1 10:15 下午
 */
abstract class ScriptAction<T> : QuestAction<T>() {

    override fun process(frame: ScriptFrame): CompletableFuture<T> {
        return run(frame)
    }

    abstract fun run(frame: ScriptFrame): CompletableFuture<T>

    override fun toString(): String {
        return "${javaClass.simpleName}()"
    }
}