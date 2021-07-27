package taboolib.module.database

/**
 * TabooLib
 * taboolib.module.database.WhereData
 *
 * @author sky
 * @since 2021/6/23 11:47 上午
 */
data class WhereData(val query: String, val value: List<Any> = emptyList(), val children: List<WhereData> = emptyList())