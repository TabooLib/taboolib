package io.izzel.tlibkotlin

import io.izzel.taboolib.TabooLib
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitTask

/**
 * @author Arasple
 * @date 2020/2/27 9:30
 */
object Tasks {

    fun run(async: Boolean = false, delay: Long = 0, period: Long = 0L, runnable: () -> (Unit)): BukkitTask {
        return when {
            period > 0 -> timer(delay, period, async, runnable)
            delay > 0 -> delay(delay, async, runnable)
            else -> task(async, runnable)
        }
    }

    fun task(runnable: () -> (Unit)): BukkitTask {
        return task(false, runnable)
    }

    fun task(async: Boolean, runnable: () -> (Unit)): BukkitTask {
        return if (async) {
            Bukkit.getScheduler().runTaskAsynchronously(TabooLib.getPlugin(), runnable.toRunnable())
        } else {
            Bukkit.getScheduler().runTask(TabooLib.getPlugin(), runnable.toRunnable())
        }
    }

    fun delay(delay: Long, runnable: () -> (Unit)): BukkitTask {
        return delay(delay, false, runnable)
    }

    fun delay(delay: Long, async: Boolean, runnable: () -> (Unit)): BukkitTask {
        return if (async) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(TabooLib.getPlugin(), runnable.toRunnable(), delay)
        } else {
            Bukkit.getScheduler().runTaskLater(TabooLib.getPlugin(), runnable.toRunnable(), delay)
        }
    }

    fun timer(delay: Long, period: Long, runnable: () -> (Unit)): BukkitTask {
        return timer(delay, period, false, runnable)
    }

    fun timer(delay: Long, period: Long, async: Boolean, runnable: () -> (Unit)): BukkitTask {
        return if (async) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(TabooLib.getPlugin(), runnable.toRunnable(), delay, period)
        } else {
            Bukkit.getScheduler().runTaskTimer(TabooLib.getPlugin(), runnable.toRunnable(), delay, period)
        }
    }

    private fun (() -> (Unit)).toRunnable() = Runnable {
        try {
            this.invoke()
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }
}