package taboolib.common.io

/**
 * taboolib 字符串
 */
val taboolibId: String
    get() = charArrayOf('t', 'a', 'b', 'o', 'o', 'l', 'i', 'b').concatToString()

/**
 * 组名（项目标识）
 * 例如：org.tabooproject
 */
val groupId = "taboolib".substringBefore(".$taboolibId")
    get() {
        if (field == "taboolib") return System.getProperty("taboolib.group", field)
        return field
    }

/**
 * taboolib 路径
 * 例如：org.tabooproject.taboolib
 */
val taboolibPath: String
    get() = "$groupId.$taboolibId"

/**
 * 特定类的组名
 */
val Class<*>.groupId: String
    get() = name.substringBefore(taboolibId).dropLast(1)