package taboolib.common.util

import taboolib.common.io.digest
import java.io.File
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@OptIn(ExperimentalTime::class)
fun main() {
    val file = File("C:/Users/sky/Desktop/Code/Adyeshach/plugin/build/libs/Adyeshach-2.0.25.jar")
    println(measureTime {
        repeat(1) { file.digest("sha-1") }
    })
    println(measureTime {
        repeat(100) { file.digest("sha-1") }
    })
}