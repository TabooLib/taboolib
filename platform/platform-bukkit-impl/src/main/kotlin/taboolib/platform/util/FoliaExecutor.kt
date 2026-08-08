package taboolib.platform.util

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import taboolib.common.platform.function.submit as submitPlatform
import taboolib.common.platform.service.PlatformExecutor
import taboolib.platform.BukkitExecutor
import taboolib.platform.BukkitPlugin
import taboolib.platform.Folia
import taboolib.platform.FoliaExecutor
import java.util.concurrent.CompletableFuture

/**
 * 在 Bukkit 主线程或 Folia 全局区域线程执行不属于具体实体、区块或位置的任务。
 */
@JvmOverloads
fun submitGlobal(
    now: Boolean = false,
    delay: Long = 0,
    period: Long = 0,
    executor: PlatformExecutor.PlatformTask.() -> Unit,
): PlatformExecutor.PlatformTask {
    if (!Folia.isFolia) {
        val runNow = now && Bukkit.isPrimaryThread()
        return submitPlatform(runNow, false, if (now) 0 else delay, if (now) 0 else period, executor)
    }
    val scheduledTask = if (now || period < 1) {
        if (now || delay < 1) {
            FoliaExecutor.GLOBAL_REGION_SCHEDULER.run(BukkitPlugin.getInstance()) { task ->
                executor(BukkitExecutor.BukkitPlatformTask { task.cancel() })
            }
        } else {
            FoliaExecutor.GLOBAL_REGION_SCHEDULER.runDelayed(BukkitPlugin.getInstance(), { task ->
                executor(BukkitExecutor.BukkitPlatformTask { task.cancel() })
            }, delay.coerceAtLeast(1))
        }
    } else {
        FoliaExecutor.GLOBAL_REGION_SCHEDULER.runAtFixedRate(BukkitPlugin.getInstance(), { task ->
            executor(BukkitExecutor.BukkitPlatformTask { task.cancel() })
        }, delay.coerceAtLeast(1), period)
    }
    return BukkitExecutor.BukkitPlatformTask { scheduledTask.cancel() }
}

// ============================================
// Location 扩展函数
// ============================================

/**
 * 在指定位置所属的 Folia 区域线程中执行回调并返回结果。
 */
fun <T> Location.callRegion(executor: () -> T): T {
    check(isOwnedByCurrentRegion()) {
        "The current thread does not own this location. Use Location.callRegionAsync(), runTask(), or submit() instead."
    }
    return executor()
}

/**
 * 在指定位置所属线程中执行回调，并通过 Future 非阻塞返回结果。
 */
fun <T> Location.callRegionAsync(executor: () -> T): CompletableFuture<T> {
    val future = CompletableFuture<T>()
    if (isOwnedByCurrentRegion()) {
        future.completeWith(executor)
    } else if (Folia.isFolia) {
        FoliaExecutor.REGION_SCHEDULER.run(BukkitPlugin.getInstance(), this) {
            future.completeWith(executor)
        }
    } else {
        submitPlatform { future.completeWith(executor) }
    }
    return future
}

/**
 * 在指定位置执行一个任务（支持 Folia 区域调度）
 *
 * @param executor 任务
 * @param useScheduler 在非 Folia 环境下是否使用调度器（默认 true）
 */
@JvmOverloads
fun Location.runTask(executor: Runnable, useScheduler: Boolean = true): PlatformExecutor.PlatformTask {
    // 如果不是 Folia 环境
    if (!Folia.isFolia) {
        return if (useScheduler) {
            submitPlatform(now = false, async = false, delay = 0, period = 0) { executor.run() }
        } else {
            executor.run()
            BukkitExecutor.BukkitPlatformTask { }
        }
    }

    // Folia 环境下，使用 RegionScheduler
    val scheduledTask = FoliaExecutor.REGION_SCHEDULER.run(BukkitPlugin.getInstance(), this) {
        executor.run()
    }

    return BukkitExecutor.BukkitPlatformTask { scheduledTask.cancel() }
}

/**
 * 在指定位置注册一个调度器（支持 Folia 区域调度）
 *
 * @param now 是否立即执行
 * @param async 是否异步执行（如果为 true，将使用全局调度器）
 * @param delay 延迟执行时间（tick）
 * @param period 重复执行时间（tick）
 * @param useScheduler 在非 Folia 环境下是否使用调度器（默认 true）
 * @param executor 调度器具体行为
 */
@JvmOverloads
fun Location.submit(
    now: Boolean = false,
    async: Boolean = false,
    delay: Long = 0,
    period: Long = 0,
    useScheduler: Boolean = true,
    executor: PlatformExecutor.PlatformTask.() -> Unit,
): PlatformExecutor.PlatformTask {
    // 如果不是 Folia 环境
    if (!Folia.isFolia) {
        return if (useScheduler || async) {
            val runNow = now && (async || Bukkit.isPrimaryThread())
            submitPlatform(runNow, async, if (now) 0 else delay, if (now) 0 else period, executor)
        } else {
            val task = BukkitExecutor.BukkitPlatformTask { }
            if (now) {
                executor(task)
            }
            task
        }
    }

    // 如果是异步执行，使用原来的 submit
    if (async) {
        return submitPlatform(now, async, delay, period, executor)
    }

    // Folia 环境下，使用 RegionScheduler 在指定位置执行
    var scheduledTask: ScheduledTask? = null

    if (now && isOwnedByCurrentRegion()) {
        // 当前线程拥有该区域时立即执行
        val task = BukkitExecutor.BukkitPlatformTask { scheduledTask?.cancel() }
        executor(task)
        return task
    }

    // 延迟或定时执行
    scheduledTask = if (now || period < 1) {
        // 单次执行
        if (now || delay < 1) {
            FoliaExecutor.REGION_SCHEDULER.run(BukkitPlugin.getInstance(), this) { task ->
                val platformTask = BukkitExecutor.BukkitPlatformTask { task.cancel() }
                executor(platformTask)
            }
        } else {
            FoliaExecutor.REGION_SCHEDULER.runDelayed(BukkitPlugin.getInstance(), this, { task ->
                val platformTask = BukkitExecutor.BukkitPlatformTask { task.cancel() }
                executor(platformTask)
            }, delay.coerceAtLeast(1))
        }
    } else {
        // 重复执行
        FoliaExecutor.REGION_SCHEDULER.runAtFixedRate(BukkitPlugin.getInstance(), this, { task ->
            val platformTask = BukkitExecutor.BukkitPlatformTask { task.cancel() }
            executor(platformTask)
        }, delay.coerceAtLeast(1), period)
    }

    return BukkitExecutor.BukkitPlatformTask { scheduledTask.cancel() }
}

// ============================================
// Entity 扩展函数
// ============================================

/**
 * 在实体所属的 Folia 实体线程中执行回调并返回结果。
 */
fun <T> Entity.callRegion(executor: () -> T): T {
    check(isOwnedByCurrentRegion()) {
        "The current thread does not own this entity. Use Entity.callRegionAsync(), runTask(), or submit() instead."
    }
    return executor()
}

/**
 * 在实体所属线程中执行回调，并通过 Future 非阻塞返回结果。
 */
fun <T> Entity.callRegionAsync(executor: () -> T): CompletableFuture<T> {
    val future = CompletableFuture<T>()
    if (isOwnedByCurrentRegion()) {
        future.completeWith(executor)
    } else if (Folia.isFolia) {
        val scheduledTask = FoliaExecutor.getEntityScheduler(this).run(BukkitPlugin.getInstance(), {
            future.completeWith(executor)
        }, {
            future.completeExceptionally(IllegalStateException("Entity scheduler retired."))
        })
        if (scheduledTask == null && !future.isDone) {
            future.completeExceptionally(IllegalStateException("Entity scheduler rejected task."))
        }
    } else {
        submitPlatform { future.completeWith(executor) }
    }
    return future
}

/**
 * 在实体所在位置执行一个任务（Folia 安全）
 *
 * @param executor 任务
 * @param useScheduler 在非 Folia 环境下是否使用调度器（默认 true）
 */
@JvmOverloads
fun Entity.runTask(executor: Runnable, useScheduler: Boolean = true): PlatformExecutor.PlatformTask {
    // 如果不是 Folia 环境
    if (!Folia.isFolia) {
        return if (useScheduler) {
            submitPlatform(now = false, async = false, delay = 0, period = 0) { executor.run() }
        } else {
            executor.run()
            BukkitExecutor.BukkitPlatformTask { }
        }
    }

    // Folia 环境下，使用 Entity Scheduler
    val entityScheduler = FoliaExecutor.getEntityScheduler(this)
    val scheduledTask = entityScheduler.run(BukkitPlugin.getInstance(), {
        executor.run()
    }, null)

    return BukkitExecutor.BukkitPlatformTask { scheduledTask?.cancel() }
}

/**
 * 在实体所在位置注册一个调度器（Folia 安全）
 *
 * @param now 是否立即执行
 * @param async 是否异步执行
 * @param delay 延迟执行时间（tick）
 * @param period 重复执行时间（tick）
 * @param useScheduler 在非 Folia 环境下是否使用调度器（默认 true）
 * @param executor 调度器具体行为
 */
@JvmOverloads
fun Entity.submit(
    now: Boolean = false,
    async: Boolean = false,
    delay: Long = 0,
    period: Long = 0,
    useScheduler: Boolean = true,
    executor: PlatformExecutor.PlatformTask.() -> Unit,
): PlatformExecutor.PlatformTask {
    // 如果不是 Folia 环境
    if (!Folia.isFolia) {
        return if (useScheduler || async) {
            val runNow = now && (async || Bukkit.isPrimaryThread())
            submitPlatform(runNow, async, if (now) 0 else delay, if (now) 0 else period, executor)
        } else {
            val task = BukkitExecutor.BukkitPlatformTask { }
            if (now) {
                executor(task)
            }
            task
        }
    }

    // 如果是异步执行，使用原来的 submit
    if (async) {
        return submitPlatform(now, async, delay, period, executor)
    }

    // Folia 环境下，使用 Entity Scheduler
    var scheduledTask: ScheduledTask? = null

    if (now && isOwnedByCurrentRegion()) {
        // 当前线程拥有该实体时立即执行
        val task = BukkitExecutor.BukkitPlatformTask { scheduledTask?.cancel() }
        executor(task)
        return task
    }

    // 获取 Entity Scheduler
    val entityScheduler = FoliaExecutor.getEntityScheduler(this)

    // 延迟或定时执行
    scheduledTask = if (now || period < 1) {
        // 单次执行
        if (now || delay < 1) {
            entityScheduler.run(BukkitPlugin.getInstance(), { task ->
                val platformTask = BukkitExecutor.BukkitPlatformTask { task.cancel() }
                executor(platformTask)
            }, null)
        } else {
            entityScheduler.runDelayed(BukkitPlugin.getInstance(), { task ->
                val platformTask = BukkitExecutor.BukkitPlatformTask { task.cancel() }
                executor(platformTask)
            }, null, delay.coerceAtLeast(1))
        }
    } else {
        // 重复执行
        entityScheduler.runAtFixedRate(BukkitPlugin.getInstance(), { task ->
            val platformTask = BukkitExecutor.BukkitPlatformTask { task.cancel() }
            executor(platformTask)
        }, null, delay.coerceAtLeast(1), period)
    }

    return BukkitExecutor.BukkitPlatformTask { scheduledTask?.cancel() }
}

// ============================================
// Block 扩展函数
// ============================================

/**
 * 在方块所属的 Folia 区域线程中执行回调并返回结果。
 */
fun <T> Block.callRegion(executor: () -> T): T {
    return location.callRegion(executor)
}

/**
 * 在方块所属线程中执行回调，并通过 Future 非阻塞返回结果。
 */
fun <T> Block.callRegionAsync(executor: () -> T): CompletableFuture<T> {
    return location.callRegionAsync(executor)
}

/**
 * 在方块所在位置执行一个任务（Folia 安全）
 *
 * @param executor 任务
 * @param useScheduler 在非 Folia 环境下是否使用调度器（默认 true）
 */
@JvmOverloads
fun Block.runTask(executor: Runnable, useScheduler: Boolean = true): PlatformExecutor.PlatformTask {
    return location.runTask(executor, useScheduler)
}

/**
 * 在方块所在位置注册一个调度器（Folia 安全）
 *
 * @param now 是否立即执行
 * @param async 是否异步执行
 * @param delay 延迟执行时间（tick）
 * @param period 重复执行时间（tick）
 * @param useScheduler 在非 Folia 环境下是否使用调度器（默认 true）
 * @param executor 调度器具体行为
 */
@JvmOverloads
fun Block.submit(
    now: Boolean = false,
    async: Boolean = false,
    delay: Long = 0,
    period: Long = 0,
    useScheduler: Boolean = true,
    executor: PlatformExecutor.PlatformTask.() -> Unit,
): PlatformExecutor.PlatformTask {
    return location.submit(now, async, delay, period, useScheduler, executor)
}

// ============================================
// Chunk 扩展函数
// ============================================

/**
 * 在区块中心所属的 Folia 区域线程中执行回调并返回结果。
 */
fun <T> Chunk.callRegion(executor: () -> T): T {
    return Location(world, (x shl 4) + 8.0, 64.0, (z shl 4) + 8.0).callRegion(executor)
}

/**
 * 在区块中心所属线程中执行回调，并通过 Future 非阻塞返回结果。
 */
fun <T> Chunk.callRegionAsync(executor: () -> T): CompletableFuture<T> {
    return Location(world, (x shl 4) + 8.0, 64.0, (z shl 4) + 8.0).callRegionAsync(executor)
}

/**
 * 在区块中心位置执行一个任务（Folia 安全）
 *
 * @param executor 任务
 * @param useScheduler 在非 Folia 环境下是否使用调度器（默认 true）
 */
@JvmOverloads
fun Chunk.runTask(executor: Runnable, useScheduler: Boolean = true): PlatformExecutor.PlatformTask {
    val location = Location(world, (x shl 4) + 8.0, 64.0, (z shl 4) + 8.0)
    return location.runTask(executor, useScheduler)
}

/**
 * 在区块中心位置注册一个调度器（Folia 安全）
 *
 * @param now 是否立即执行
 * @param async 是否异步执行
 * @param delay 延迟执行时间（tick）
 * @param period 重复执行时间（tick）
 * @param useScheduler 在非 Folia 环境下是否使用调度器（默认 true）
 * @param executor 调度器具体行为
 */
@JvmOverloads
fun Chunk.submit(
    now: Boolean = false,
    async: Boolean = false,
    delay: Long = 0,
    period: Long = 0,
    useScheduler: Boolean = true,
    executor: PlatformExecutor.PlatformTask.() -> Unit,
): PlatformExecutor.PlatformTask {
    val location = Location(world, (x shl 4) + 8.0, 64.0, (z shl 4) + 8.0)
    return location.submit(now, async, delay, period, useScheduler, executor)
}

// ============================================
// World 扩展函数（坐标）
// ============================================

/**
 * 在指定世界坐标所属的 Folia 区域线程中执行回调并返回结果。
 */
fun <T> World.callRegion(x: Double, z: Double, executor: () -> T): T {
    return Location(this, x, 64.0, z).callRegion(executor)
}

/**
 * 在指定世界坐标所属线程中执行回调，并通过 Future 非阻塞返回结果。
 */
fun <T> World.callRegionAsync(x: Double, z: Double, executor: () -> T): CompletableFuture<T> {
    return Location(this, x, 64.0, z).callRegionAsync(executor)
}

/**
 * 在指定世界方块坐标所属的 Folia 区域线程中执行回调并返回结果。
 */
fun <T> World.callRegion(x: Int, y: Int, z: Int, executor: () -> T): T {
    return Location(this, x.toDouble(), y.toDouble(), z.toDouble()).callRegion(executor)
}

/**
 * 在指定世界方块坐标所属线程中执行回调，并通过 Future 非阻塞返回结果。
 */
fun <T> World.callRegionAsync(x: Int, y: Int, z: Int, executor: () -> T): CompletableFuture<T> {
    return Location(this, x.toDouble(), y.toDouble(), z.toDouble()).callRegionAsync(executor)
}

/**
 * 在指定世界坐标执行一个任务（Folia 安全）
 *
 * @param x X 坐标
 * @param z Z 坐标
 * @param executor 任务
 * @param useScheduler 在非 Folia 环境下是否使用调度器（默认 true）
 */
@JvmOverloads
fun World.runTask(x: Double, z: Double, executor: Runnable, useScheduler: Boolean = true): PlatformExecutor.PlatformTask {
    val location = Location(this, x, 64.0, z)
    return location.runTask(executor, useScheduler)
}

/**
 * 在指定世界坐标注册一个调度器（Folia 安全）
 *
 * @param x X 坐标
 * @param z Z 坐标
 * @param now 是否立即执行
 * @param async 是否异步执行
 * @param delay 延迟执行时间（tick）
 * @param period 重复执行时间（tick）
 * @param useScheduler 在非 Folia 环境下是否使用调度器（默认 true）
 * @param executor 调度器具体行为
 */
@JvmOverloads
fun World.submit(
    x: Double,
    z: Double,
    now: Boolean = false,
    async: Boolean = false,
    delay: Long = 0,
    period: Long = 0,
    useScheduler: Boolean = true,
    executor: PlatformExecutor.PlatformTask.() -> Unit,
): PlatformExecutor.PlatformTask {
    val location = Location(this, x, 64.0, z)
    return location.submit(now, async, delay, period, useScheduler, executor)
}

/**
 * 判断当前线程是否拥有该位置所属的区域。
 *
 * 非 Folia 服务端没有区域概念，「当前线程是否拥有该位置」在语义上不适用，因此恒返回 true。
 * 若需要判断是否处于主线程，请显式使用 [org.bukkit.Bukkit.isPrimaryThread]。
 */
fun Location.isOwnedByCurrentRegion(): Boolean {
    if (!Folia.isFolia) {
        return true
    }
    return kotlin.runCatching {
        Bukkit::class.java.invokeMethod<Boolean>("isOwnedByCurrentRegion", this, isStatic = true, remap = false) == true
    }.getOrDefault(false)
}

/**
 * 判断当前线程是否拥有该实体所属的区域。
 *
 * 非 Folia 服务端没有区域概念，「当前线程是否拥有该实体」在语义上不适用，因此恒返回 true。
 * 若需要判断是否处于主线程，请显式使用 [org.bukkit.Bukkit.isPrimaryThread]。
 */
fun Entity.isOwnedByCurrentRegion(): Boolean {
    if (!Folia.isFolia) {
        return true
    }
    return kotlin.runCatching {
        Bukkit::class.java.invokeMethod<Boolean>("isOwnedByCurrentRegion", this, isStatic = true, remap = false) == true
    }.getOrDefault(false)
}

private fun <T> CompletableFuture<T>.completeWith(executor: () -> T) {
    try {
        complete(executor())
    } catch (throwable: Throwable) {
        completeExceptionally(throwable)
    }
}
