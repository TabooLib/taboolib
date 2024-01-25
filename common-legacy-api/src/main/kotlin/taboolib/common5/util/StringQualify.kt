package taboolib.common5.util

/**
 * 将文字转换为打印机特效
 */
fun String.printed(separator: String = ""): List<String> {
    val result = ArrayList<String>()
    var i = 0
    while (i < length) {
        if (get(i) == '§') {
            i++
        } else {
            result.add("${substring(0, i + 1)}${if (i % 2 == 1) separator else ""}")
        }
        i++
    }
    if (separator.isNotEmpty() && i % 2 == 0) {
        result.add(this)
    }
    return result
}

/**
 * 生成百分比进度条
 *
 * @param empty 空
 * @param fill 填充
 * @param length 长度
 * @param percent 百分比
 */
fun createBar(empty: String, fill: String, length: Int, percent: Double): String {
    return (1..length).joinToString("") {
        if (percent.isNaN() || percent == 0.0) empty else if (percent >= it.toDouble() / length) fill else empty
    }
}