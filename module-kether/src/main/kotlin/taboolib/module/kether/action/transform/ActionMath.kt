package taboolib.module.kether.action.transform

import taboolib.common5.Coerce
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * ink.ptms.zaphkiel.module.kether.ActionMath
 *
 * @author sky
 * @since 2021/3/16 2:56 下午
 */
class ActionMath(val type: Type, val array: List<ParsedAction<*>>) : ScriptAction<Number>() {

    enum class Type(val exec: List<Any>.() -> Number) {

        ADD({
            if (intAll()) {
                sumBy { Coerce.toInteger(it) }
            } else {
                sumByDouble { Coerce.toDouble(it) }
            }
        }),

        SUB({
            if (intAll()) {
                subBy { Coerce.toInteger(it) }
            } else {
                subByDouble { Coerce.toDouble(it) }
            }
        }),

        MUL({
            if (intAll()) {
                mulBy { Coerce.toInteger(it) }
            } else {
                mulByDouble { Coerce.toDouble(it) }
            }
        }),

        DIV({
            if (intAll()) {
                divBy { Coerce.toInteger(it) }
            } else {
                divByDouble { Coerce.toDouble(it) }
            }
        });

        companion object {

            fun List<Any>.intAll() = all { it is Int || it.isInt() }

            fun fromString(value: String): Type? {
                return when (value) {
                    "add", "+" -> ADD
                    "sub", "-" -> SUB
                    "mul", "*" -> MUL
                    "div", "/" -> DIV
                    else -> null
                }
            }
        }
    }

    override fun run(frame: ScriptFrame): CompletableFuture<Number> {
        val future = CompletableFuture<Number>()
        val number = ArrayList<Any>()
        fun process(cur: Int) {
            if (cur < array.size) {
                frame.newFrame(array[cur]).run<Any>().thenApply {
                    number.add(it)
                    if (frame.script().breakLoop) {
                        frame.script().breakLoop = false
                        future.complete(type.exec(number))
                    } else {
                        process(cur + 1)
                    }
                }
            } else {
                future.complete(type.exec(number))
            }
        }
        process(0)
        return future
    }

    internal object Parser {

        val math = arrayOf(arrayOf("add", "+"), arrayOf("sub", "-"), arrayOf("mul", "*"), arrayOf("div", "/"))
        val mathGroup = math.flatten().toTypedArray()

        /**
         * math + [ 1 2 3 ]
         * math *1 + *1 - *10
         */
        @KetherParser(["math"])
        fun parser0() = scriptParser {
            it.switch {
                case(*math[0]) {
                    ActionMath(Type.ADD, it.next(ArgTypes.listOf(ArgTypes.ACTION)))
                }
                case(*math[1]) {
                    ActionMath(Type.SUB, it.next(ArgTypes.listOf(ArgTypes.ACTION)))
                }
                case(*math[2]) {
                    ActionMath(Type.MUL, it.next(ArgTypes.listOf(ArgTypes.ACTION)))
                }
                case(*math[3]) {
                    ActionMath(Type.DIV, it.next(ArgTypes.listOf(ArgTypes.ACTION)))
                }
                other {
                    val stack = ArrayList<MathStack>()
                    stack += MathStack(it.next(ArgTypes.ACTION))
                    fun check(): Boolean {
                        return try {
                            mark()
                            val symbol = it.expects(*mathGroup)
                            val num = it.next(ArgTypes.ACTION)
                            stack += MathStack(num, Type.fromString(symbol))
                            check()
                            true
                        } catch (ex: Exception) {
                            reset()
                            false
                        }
                    }
                    check()
                    if (stack.size > 1) {
                        actionFuture {
                            newFrame(stack[0].action).run<Any>().thenApply { base ->
                                var num = base.inferType() as Number
                                fun process(cur: Int) {
                                    if (cur < stack.size) {
                                        newFrame(stack[cur].action).run<Any>().thenApply { num2 ->
                                            num = stack[cur].symbol!!.exec(listOf(num, num2))
                                            process(cur + 1)
                                        }
                                    } else {
                                        it.complete(num)
                                    }
                                }
                                process(1)
                            }
                        }
                    } else {
                        null
                    }
                }
            }
        }

        class MathStack(val action: ParsedAction<*>, val symbol: Type? = null)
    }
}