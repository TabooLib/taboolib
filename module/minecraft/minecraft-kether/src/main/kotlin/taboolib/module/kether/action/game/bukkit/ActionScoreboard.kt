package taboolib.module.kether.action.game.bukkit

import org.bukkit.entity.Player
import taboolib.common.Inject
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.function.submit
import taboolib.common.util.asList
import taboolib.module.kether.*
import taboolib.module.nms.sendScoreboard
import java.util.concurrent.CancellationException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
import java.util.concurrent.atomic.AtomicReference

@Inject
@PlatformSide(Platform.BUKKIT)
object ActionScoreboard {

    @KetherParser(["scoreboard"])
    fun actionScoreboard() = scriptParser {
        val value = it.nextParsedAction()
        actionTake {
            val viewer = player().cast<Player>()
            val result = CompletableFuture<Any?>()
            val updateFuture = AtomicReference<CompletableFuture<Any?>?>()
            val contentFuture = run(value)
            contentFuture.whenComplete { content, ex ->
                if (ex != null) {
                    completeFailure(result, ex)
                } else if (!result.isDone) {
                    val scoreboardFuture = updateScoreboard(viewer, content)
                    updateFuture.set(scoreboardFuture)
                    if (result.isCancelled) {
                        scoreboardFuture.cancel(false)
                    } else {
                        scoreboardFuture.whenComplete { _, updateEx ->
                            if (updateEx != null) {
                                completeFailure(result, updateEx)
                            } else {
                                result.complete(null)
                            }
                        }
                    }
                }
            }
            result.whenComplete { _, _ ->
                if (result.isCancelled) {
                    contentFuture.cancel(false)
                    updateFuture.get()?.cancel(false)
                }
            }
            result
        }
    }

    private fun completeFailure(future: CompletableFuture<*>, throwable: Throwable) {
        var cause = throwable
        while (cause is CompletionException) {
            val nested = cause.cause ?: break
            cause = nested
        }
        if (cause is CancellationException) {
            future.cancel(false)
        } else {
            future.completeExceptionally(cause)
        }
    }

    private fun updateScoreboard(viewer: Player, content: Any?): CompletableFuture<Any?> {
        val future = CompletableFuture<Any?>()
        try {
            val task = submit {
                if (future.isCancelled) {
                    return@submit
                }
                try {
                    val body = when (content) {
                        null -> emptyList()
                        is Collection<*>, is Array<*> -> content.asList()
                        else -> content.toString().trimIndent().lines()
                    }
                    if (body.isEmpty()) {
                        viewer.sendScoreboard()
                    } else {
                        viewer.sendScoreboard(body.first(), *body.drop(1).toTypedArray())
                    }
                    future.complete(null)
                } catch (ex: Throwable) {
                    future.completeExceptionally(ex)
                }
            }
            future.whenComplete { _, _ ->
                if (future.isCancelled) {
                    task.cancel()
                }
            }
        } catch (ex: Throwable) {
            future.completeExceptionally(ex)
        }
        return future
    }
}
