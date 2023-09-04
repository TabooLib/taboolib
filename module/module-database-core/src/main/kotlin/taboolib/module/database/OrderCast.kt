package taboolib.module.database

/**
 * @author sky
 * @since 2019-10-26 14:02
 */
class OrderCast(val row: String, val cast: String, val desc: Boolean = false) {

    val query: String
        get() {
            return "CAST(`${row.replace(".", "`.`")}` AS ${cast}) ${if (desc) "DESC" else "ASC"}"
        }
}
