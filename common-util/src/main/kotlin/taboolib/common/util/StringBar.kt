package taboolib.common.util

/**
 * 基于模板构建一个自定义 Bar（进度条）
 *
 * 这个函数用于创建一个可自定义的进度条，允许你使用模板字符串来定义进度条的外观。
 * 模板字符串的格式应为 "prefix(body)suffix"，其中 prefix 和 suffix 分别表示进度条的左右两端，
 * body 表示进度条的主体部分。
 *
 * 使用示例：
 * ```
 * val bar = buildBarWith("【(=)】", 0.75, 10)
 * println(bar)  // 输出：【===== 】
 * ```
 *
 * 你还可以使用 builder 参数来进一步自定义每个字符的显示：
 * ```
 * val coloredBar = buildBarWith("0(#)9", 0.6, 20) { _, code ->
 *     when (code) {
 *         "0", "9" -> code.red()
 *         "#" -> code.green()
 *         else -> code
 *     }
 * }
 * println(coloredBar)  // 输出：带有颜色的进度条
 * ```
 *
 * @param template 模板字符串，格式为 "prefix(body)suffix"
 * @param value 当前值（0.0 到 1.0 之间的浮点数）
 * @param length Bar 的总长度
 * @param reverse 是否逆序构建 Bar
 *                如果为 true，则从右到左构建；如果为 false，则从左到右构建
 * @param builder 可选的构建函数，用于进一步自定义每个字符的显示
 *                默认为 { it }，即直接返回原字符
 * @return 构建好的 Bar 字符串
 */
fun buildStringBarWith(template: String, value: Double, length: Int, reverse: Boolean = false, builder: (index: Int, code: String) -> String = { _, code -> code }): String {
    val (prefix, body, suffix) = parseTemplate(template)
    return buildStringBar(value, length, reverse) { index, state ->
        val code = if (state) {
            when {
                // 左侧
                index < prefix.length -> prefix[index].toString()
                // 右侧
                index >= length - suffix.length -> suffix[index - (length - suffix.length)].toString()
                // 中间
                else -> body
            }
        } else {
            " "
        }
        builder(index, code)
    }
}

/**
 * 构建一个 Bar（进度条）
 *
 * 这个函数用于创建一个可自定义的进度条。它允许你控制进度条的长度、填充程度，
 * 以及每个位置显示的字符。你还可以选择是从左到右还是从右到左构建进度条。
 *
 * 使用示例：
 * ```
 * val bar = buildBar(0.75, 10) { index, state ->
 *     if (state) "#" else "-"
 * }
 * println(bar)  // 输出：#######---
 * ```
 *
 * 你也可以创建更复杂的进度条，比如带有不同边界字符的进度条：
 * ```
 * val complexBar = buildBar(0.6, 20, reverse = true) { index, state ->
 *     when {
 *         index == 0 -> "["
 *         index == 19 -> "]"
 *         state -> "="
 *         else -> " "
 *     }
 * }
 * println(complexBar)  // 输出：[    ============]
 * ```
 *
 * @param value 当前值（0.0 到 1.0 之间的浮点数）
 * @param length Bar 的总长度
 * @param reverse 是否逆序构建 Bar
 *                如果为 true，则从右到左构建；如果为 false，则从左到右构建
 * @param builder 用于构建每个部分的函数，接收两个参数：
 *                index：当前索引（0 到 length - 1）
 *                state：状态（true 表示填充，false 表示空）
 *                这个函数决定了进度条每个位置显示的字符
 * @return 构建好的 Bar 字符串
 */
fun buildStringBar(value: Double, length: Int, reverse: Boolean = false, builder: (index: Int, state: Boolean) -> String): String {
    val range = if (reverse) (length - 1 downTo 0) else (0 until length)
    return range.joinToString("") { i ->
        builder(
            // 如果是逆序，需要调整索引
            if (reverse) length - 1 - i else i,
            // 判断当前位置是否应该被填充
            if (value != 0.0) {
                // 计算当前位置的百分比是否小于等于给定的 value
                i.toDouble() / (length - 1) <= value
            } else {
                // 如果 value 为 0，则所有位置都为空
                false
            }
        )
    }
}

private fun parseTemplate(template: String): Triple<String, String, String> {
    val parts = template.split("(", ")")
    require(parts.size == 3) { "模板格式不正确，应为 'prefix(body)suffix'" }
    return Triple(parts[0], parts[1], parts[2])
}
