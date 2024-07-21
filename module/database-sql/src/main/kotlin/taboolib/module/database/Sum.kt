package taboolib.module.database

/**
 * 求和
 *
 * @author ray_hughes
 */
class Sum(val row: String, val asRow: String) : Attributes {


    /**
     *  截断结果小数位 TRUNCATE
     */
    var truncate: Int = -1

    /** 语句 */
    override val query: String
        get() {
            if (truncate > -1) {
                return "TRUNCATE(SUM(${row.asFormattedColumnName()}), $truncate) AS ${asRow.asFormattedColumnName()}"
            }
            return "SUM(${row.asFormattedColumnName()}) AS ${asRow.asFormattedColumnName()}"
        }
}
