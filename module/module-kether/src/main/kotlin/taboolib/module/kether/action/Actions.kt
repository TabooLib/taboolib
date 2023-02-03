package taboolib.module.kether.action

import org.tabooproject.reflex.Reflex.Companion.getProperty
import taboolib.common.platform.function.*
import taboolib.common5.Coerce
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestFuture
import taboolib.module.kether.*
import taboolib.module.kether.ParserHolder.option
import java.util.concurrent.CompletableFuture

internal object Actions {

    @KetherParser(["import"])
    fun actionImport() = scriptParser {
        it.getProperty<MutableList<String>>("namespace")!!.add(it.nextToken())
        actionNow { null }
    }

    @KetherParser(["release"])
    fun actionRelease() = scriptParser {
        it.getProperty<MutableList<String>>("namespace")!!.remove(it.nextToken())
        actionNow { null }
    }

    @KetherParser(["pause"])
    fun actionPause() = scriptParser {
        actionFuture { }
    }

    @KetherParser(["exit", "stop", "terminate"])
    fun actionExit() = scriptParser {
        actionNow { ScriptService.terminateQuest(script()) }
    }

    @KetherParser(["log", "print", "info"])
    fun actionInfo() = combinationParser {
        it.group(text()).apply(it) { str -> now { info(str) } }
    }

    @KetherParser(["warn", "warning"])
    fun actionWarning() = combinationParser {
        it.group(text()).apply(it) { str -> now { warning(str) } }
    }

    @KetherParser(["error", "severe"])
    fun actionSevere() = combinationParser {
        it.group(text()).apply(it) { str -> now { severe(str) } }
    }

    @KetherParser(["wait", "delay", "sleep"])
    fun actionWait() = scriptParser {
        val ticks = it.next(ArgTypes.DURATION).toMillis() / 50L
        actionFuture { f ->
            val task = submit(delay = ticks, async = !isPrimaryThread) {
                // 如果玩家在等待过程中离线则终止脚本
                if (script().sender?.isOnline() == false) {
                    ScriptService.terminateQuest(script())
                    return@submit
                }
                f.complete(null)
            }
            addClosable(AutoCloseable { task.cancel() })
        }
    }

    @Suppress("UNCHECKED_CAST")
    @KetherParser(["async"])
    fun actionAsync() = scriptParser {
        val action = it.nextParsedAction() as ParsedAction<Any>
        actionNow { QuestFuture(action, run(action)) }
    }

    @KetherParser(["call"])
    fun actionCall() = scriptParser {
        val block = it.nextToken()
        actionTake {
            val newFrame = newFrame(block)
            val newBlock = context().quest.blocks[block] ?: error("block $block not found")
            newFrame.setNext(newBlock)
            addClosable(newFrame)
            newFrame.run<Any>()
        }
    }

    @KetherParser(["goto"])
    fun actionGoto() = scriptParser {
        val block = it.nextToken()
        actionNow { setNext(context().quest.blocks[block] ?: error("block $block not found")) }
    }

    @KetherParser(["if"])
    fun actionIf() = combinationParser {
        it.group(bool(), command("then", then = action()), command("else", then = action()).option()).apply(it) { condition, t, f ->
            future {
                if (condition) run(t) else if (f != null) run(f) else CompletableFuture.completedFuture(null)
            }
        }
    }

    @KetherParser(["not"])
    fun actionNot() = combinationParser {
        it.group(bool()).apply(it) { b -> now { !b } }
    }

    @KetherParser(["repeat"])
    fun actionRepeat() = scriptParser {
        val times = it.nextParsedAction()
        val action = it.nextParsedAction()
        actionFuture { f ->
            run(times).int { t ->
                val futures = mutableListOf<CompletableFuture<Any?>>()
                repeat(t) { futures.add(run(action)) }
                CompletableFuture.allOf(*futures.toTypedArray()).thenAccept { f.complete(null) }
            }
        }
    }

    @KetherParser(["all"])
    fun actionAll() = scriptParser {
        val actions = it.next(ArgTypes.listOf(ArgTypes.ACTION))
        actionFuture { f ->
            fun process(cur: Int) {
                if (cur < actions.size) {
                    run(actions[cur]).bool { b ->
                        if (b) {
                            process(cur + 1)
                        } else {
                            f.complete(false)
                        }
                    }
                } else {
                    f.complete(true)
                }
            }
            process(0)
        }
    }

    @KetherParser(["any"])
    fun actionAny() = scriptParser {
        val actions = it.next(ArgTypes.listOf(ArgTypes.ACTION))
        actionFuture { f ->
            fun process(cur: Int) {
                if (cur < actions.size) {
                    run(actions[cur]).bool { b ->
                        if (b) {
                            f.complete(true)
                        } else {
                            process(cur + 1)
                        }
                    }
                } else {
                    f.complete(false)
                }
            }
            process(0)
        }
    }

    @KetherParser(["await"])
    fun actionAwait() = scriptParser {
        val action = it.nextParsedAction()
        actionTake {
            val future = CompletableFuture<Any>()
            run(action).thenAccept(QuestFuture.complete(future))
            future
        }
    }

    @KetherParser(["await_all"])
    fun actionAwaitAll() = scriptParser {
        val actions = it.next(ArgTypes.listOf(ArgTypes.ACTION))
        actionTake {
            val futures = arrayOfNulls<CompletableFuture<*>>(actions.size)
            for (i in actions.indices) {
                futures[i] = run(actions[i])
            }
            CompletableFuture.allOf(*futures)
        }
    }

    @KetherParser(["await_any"])
    fun actionAwaitAny() = scriptParser {
        val actions = it.next(ArgTypes.listOf(ArgTypes.ACTION))
        actionTake {
            val futures = arrayOfNulls<CompletableFuture<*>>(actions.size)
            for (i in actions.indices) {
                futures[i] = run(actions[i])
            }
            CompletableFuture.anyOf(*futures)
        }
    }
}