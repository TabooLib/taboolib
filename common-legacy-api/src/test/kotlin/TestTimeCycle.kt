import taboolib.common5.TimeCycle
import java.util.concurrent.TimeUnit

fun main() {
    val cycle = TimeCycle(36000000)
    val start = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)
    println(start)
    println(cycle.start(start).end.timeInMillis)
}