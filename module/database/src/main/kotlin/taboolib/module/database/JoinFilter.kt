package taboolib.module.database

class JoinFilter : Filter() {

    /** 连接条件 */
    fun on(criteria: Criteria) {
        append(criteria)
    }
}