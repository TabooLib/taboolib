package taboolib.module.kether.action

import org.tabooproject.reflex.Reflex.Companion.getProperty
import taboolib.common.platform.function.*
import taboolib.common5.Coerce
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestFuture
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

object Actions {

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
        actionFuture {  }
    }

    @KetherParser(["exit", "stop", "terminate"])
    fun actionExit() = scriptParser {
        actionNow { ScriptService.terminateQuest(script()) }
    }

    @KetherParser(["log", "print", "info"])
    fun actionInfo() = scriptParser {
        val action = it.nextParsedAction()
        actionTake { run(action).str { s -> info(s) } }
    }

    @KetherParser(["warn", "warning"])
    fun actionWarning() = scriptParser {
        val action = it.nextParsedAction()
        actionTake { run(action).str { s -> warning(s) } }
    }

    @KetherParser(["error", "severe"])
    fun actionSevere() = scriptParser {
        val action = it.nextParsedAction()
        actionTake { run(action).str { s -> severe(s) } }
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
    fun actionIf() = scriptParser {
        val condition = it.nextParsedAction()
        it.expect("then")
        val trueAction = it.nextParsedAction()
        var falseAction: ParsedAction<*>? = null
        if (it.hasNext()) {
            it.mark()
            if (it.nextToken() == "else") {
                falseAction = it.nextParsedAction()
            } else {
                it.reset()
            }
        }
        actionFuture { f ->
            run(condition).bool { b ->
                if (b) {
                    run(trueAction).thenAccept { r -> f.complete(r) }
                } else if (falseAction != null) {
                    run(falseAction).thenAccept { r -> f.complete(r) }
                } else {
                    f.complete(null)
                }
            }
        }
    }

    @KetherParser(["not"])
    fun actionNot() = scriptParser {
        val condition = it.nextParsedAction()
        actionTake { run(condition).bool { b -> !b } }
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
        actionTake {
            var future = CompletableFuture.completedFuture(true)
            actions.forEach { action ->
                future = future.thenCombine(run(action)) { b, o -> b && Coerce.toBoolean(o) }
            }
            future
        }
    }

    @KetherParser(["any"])
    fun actionAny() = scriptParser {
        val actions = it.next(ArgTypes.listOf(ArgTypes.ACTION))
        actionTake {
            var future = CompletableFuture.completedFuture(false)
            actions.forEach { action ->
                future = future.thenCombine(run(action)) { b, o -> b || Coerce.toBoolean(o) }
            }
            future
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
                val action = actions[i]
                futures[i] = run(action)
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
                val action = actions[i]
                futures[i] = run(action)
            }
            CompletableFuture.anyOf(*futures)
        }
    }
}