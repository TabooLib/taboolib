package taboolib.module.database

/**
 * @author sky
 * @since 2019-10-26 14:02
 */
class Order(val row: String, val desc: Boolean = false) {

    val query: String
        get() {
            return "${row.format()} ${if (desc) "desc" else "asc"}"
        }

    private fun String.format() = "`${replace(".", "`.`")}`"
}