package taboolib.module.database

/**
 * @author 大阔
 * @since 2024/03/16 21:25
 */
data class Index(
    var name: String,
    val columns: List<String>,
    var unique: Boolean = false,
    var checkExists: Boolean = true
)
