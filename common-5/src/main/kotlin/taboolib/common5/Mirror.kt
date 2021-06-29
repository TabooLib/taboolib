package taboolib.common5

import taboolib.common.Isolated
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.util.replaceWithOrder
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

/**
 * @author 坏黑
 * @since 2018-12-24 16:32
 */
@Isolated
class Mirror {

    val dataMap = ConcurrentHashMap<String, MirrorData>()

    fun mirrorTask(id: String, func: () -> Any?): Any? {
        val time = System.nanoTime()
        val r = func()
        dataMap.computeIfAbsent(id) { MirrorData() }.finish(time)
        return r
    }

    fun mirrorFuture(id: String, func: MirrorFuture.() -> Unit) {
        func(MirrorFuture().also { mf ->
            mf.future.thenApply {
                dataMap.computeIfAbsent(id) { MirrorData() }.finish(mf.startTime)
            }
        })
    }

    fun collect(opt: Options.() -> Unit = {}): MirrorCollect {
        val options = Options("TabooLib").also(opt)
        val collect = MirrorCollect(this, options, "/", "/")
        dataMap.entries.forEach { mirror ->
            var point = collect
            mirror.key.split(":").forEach {
                point = point.sub.computeIfAbsent(it) { _ -> MirrorCollect(this, options, mirror.key, it) }
            }
        }
        return collect
    }

    fun collectAndReport(sender: ProxyCommandSender, opt: Options.() -> Unit = {}): MirrorCollect {
        return collect(opt).run {
            print(sender, getTotal(), 0)
            this
        }
    }

    class Options(val prefix: String) {

        var childFormat = "§c[$prefix] §8{0}§f{1} §8count({2})§c avg({3}ms) §7{4}ms ~ {5}ms §8··· §7{6}%"
        var parentFormat = "§c[$prefix] §8{0}§7{1} §8count({2})§c avg({3}ms) §7{4}ms ~ {5}ms §8··· §7{6}%"
    }

    class MirrorFuture {

        val startTime = System.nanoTime()
        val future = CompletableFuture<Void>()

        fun finish() {
            future.complete(null)
        }
    }

    class MirrorCollect(
        val mirror: Mirror,
        val opt: Options,
        val key: String,
        val path: String,
        val sub: MutableMap<String, MirrorCollect> = TreeMap()
    ) {

        fun getTotal(): BigDecimal {
            var total = mirror.dataMap[key]?.timeTotal ?: BigDecimal.ZERO
            sub.values.forEach {
                total = total.add(it.getTotal())
            }
            return total
        }

        fun print(sender: ProxyCommandSender, all: BigDecimal, space: Int) {
            val prefix = "${"···".repeat(space)}${if (space > 0) " " else ""}"
            val total = getTotal()
            val data = mirror.dataMap[key]
            if (data != null) {
                val count = data.count
                val avg = data.getAverage()
                val min = data.getLowest()
                val max = data.getHighest()
                val format = if (sub.isEmpty()) opt.childFormat else opt.parentFormat
                sender.sendMessage(format.replaceWithOrder(prefix, path, count, avg, min, max, percent(all, total)))
            }
            sub.values.map {
                it to percent(all, it.getTotal())
            }.sortedByDescending {
                it.second
            }.forEach {
                it.first.print(sender, all, if (data != null) space + 1 else space)
            }
        }

        fun percent(all: BigDecimal, total: BigDecimal): Double {
            return if (all.toDouble() == 0.0) 0.0 else total.divide(all, 2, RoundingMode.HALF_UP).multiply(BigDecimal("100")).toDouble()
        }
    }
}