package taboolib.module.database

class Where : WhereExecutor() {

    internal val data = ArrayList<WhereData>()

    val query: String
        get() = data.joinToString(" AND ") { it.query }

    val elements: List<Any>
        get() {
            val el = ArrayList<Any>()
            fun WhereData.push() {
                if (children.isNotEmpty()) {
                    children.forEach { it.push() }
                } else {
                    el.addAll(value)
                }
            }
            data.forEach { it.push() }
            return el
        }

    fun isEmpty(): Boolean {
        return data.isEmpty()
    }

    override fun append(whereData: WhereData) {
        data += whereData
    }
}