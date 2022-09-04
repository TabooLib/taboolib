package taboolib.module.kether.action.transform

import org.apache.commons.jexl3.JexlBuilder
import org.apache.commons.jexl3.JexlEngine
import org.apache.commons.jexl3.MapContext
import taboolib.common.platform.function.console
import taboolib.common.util.unsafeLazy
import taboolib.library.kether.QuestContext.Frame
import taboolib.module.kether.*

/**
 * TabooLib
 * taboolib.module.kether.action.transform.ActionJexl3
 *
 * @author 坏黑
 * @since 2022/9/3 16:03
 */
object ActionJexl3 {

    val jexl: JexlEngine by unsafeLazy { JexlBuilder().create() }
    var autoContext = true

    /**
     * calc dynamic ""
     * calc ""
     */
    @KetherParser(["calc", "calculate"])
    fun actionCalc() = scriptParser {
        it.mark()
        try {
            it.expects("dynamic")
            val expression = it.nextParsedAction()
            actionTake { run(expression).str { exp -> jexl.createExpression(exp).evaluate(createMapContext()) } }
        } catch (ex: Throwable) {
            it.reset()
            val expression = jexl.createExpression(it.nextToken())
            actionNow { expression.evaluate(createMapContext()) }
        }
    }

    /**
     * invoke dynamic ""
     * invoke ""
     */
    @KetherParser(["invoke"])
    fun actionInvoke() = scriptParser {
        it.mark()
        try {
            it.expects("dynamic")
            val script = it.nextParsedAction()
            actionTake { run(script).str { exp -> jexl.createScript(exp).execute(createMapContext()) } }
        } catch (ex: Throwable) {
            it.reset()
            val script = jexl.createScript(it.nextToken())
            actionNow { script.execute(createMapContext()) }
        }
    }

    fun Frame.createMapContext(): MapContext {
        val context = MapContext(deepVars())
        if (autoContext) {
            context.set("script", script())
            context.set("sender", script().sender)
            context.set("console", console())
        }
        return context
    }
}