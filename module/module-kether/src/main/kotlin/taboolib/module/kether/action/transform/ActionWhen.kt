package taboolib.module.kether.action.transform

import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * TabooLib
 * taboolib.module.kether.action.transform.ActionWhen
 *
 * @author 坏黑
 * @since 2022/9/3 17:22
 */
object ActionWhen {

    open class CaseAction(val checkType: CheckType, val condition: List<ParsedAction<*>>, val action: ParsedAction<*>) : ScriptAction<Any?>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
            error("Not Executable")
        }
    }

    class CaseDefaultAction(action: ParsedAction<*>) : CaseAction(CheckType.EQUALS, emptyList(), action)

    @KetherParser(["when"], namespace = "kether_inner:when")
    fun innerActionCase() = scriptParser {
        it.mark()
        val typeToken = it.nextToken()
        var checkType = CheckType.fromStringSafely(typeToken)
        // 无效符号
        if (checkType == null) {
            it.reset()
            checkType = CheckType.EQUALS
        }
        it.mark()
        val isList = it.nextToken() == "["
        it.reset()
        val value = if (isList) {
            it.next(ArgTypes.listOf(ArgTypes.ACTION))
        } else {
            it.reset()
            listOf(it.nextParsedAction())
        }
        it.expects("then", "->")
        CaseAction(checkType, value, it.nextParsedAction())
    }

    @KetherParser(["else"], namespace = "kether_inner:when")
    fun innerActionDefault() = scriptParser {
        CaseDefaultAction(it.nextParsedAction())
    }

    @KetherParser(["case"])
    fun actionWhen() = scriptParser {
        val input = it.nextParsedAction()
        val blocks = mutableListOf<CaseAction>()
        it.next(ArgTypes.listOf { t -> t.nextAction<Any>("kether_inner:when") }).forEach { a ->
            val action = a.action
            if (action is CaseAction) {
                blocks += action
            }
        }
        // 逻辑处理
        actionFuture { f ->
            run(input).thenAccept { input ->
                /**
                 * 处理块
                 */
                fun process(cur: Int) {
                    // 有效指针
                    if (cur < blocks.size) {
                        val caseBlock = blocks[cur]
                        // 默认块
                        if (caseBlock is CaseDefaultAction) {
                            run(caseBlock.action).thenAccept { r -> f.complete(r) }
                        } else {
                            // 包含判断
                            if (caseBlock.checkType.multi) {
                                val actions = caseBlock.condition
                                // 多值
                                val values = if (actions.size > 1) {
                                    val arr = arrayOfNulls<CompletableFuture<*>>(actions.size)
                                    for (i in actions.indices) {
                                        arr[i] = run(actions[i])
                                    }
                                    arr
                                }
                                // 单值转列表
                                else {
                                    arrayOf(run(actions[0]))
                                }
                                CompletableFuture.allOf(*values).thenAccept {
                                    if (caseBlock.checkType.check(input, if (values.size > 1) values.map { el -> el!!.get() } else values[0]!!.get())) {
                                        run(caseBlock.action).thenAccept { r -> f.complete(r) }
                                    } else {
                                        process(cur + 1)
                                    }
                                }
                            }
                            // 非包含则进行「或」判断
                            else {
                                var future = CompletableFuture.completedFuture(false)
                                caseBlock.condition.forEach { action ->
                                    future = future.thenCombine(run(action)) { b, o -> b || caseBlock.checkType.check(input, o) }
                                }
                                future.thenAccept { cond ->
                                    if (cond) {
                                        run(caseBlock.action).thenAccept { r -> f.complete(r) }
                                    } else {
                                        process(cur + 1)
                                    }
                                }
                            }
                        }
                    } else {
                        f.complete(null)
                    }
                }
                process(0)
            }
        }
    }
}