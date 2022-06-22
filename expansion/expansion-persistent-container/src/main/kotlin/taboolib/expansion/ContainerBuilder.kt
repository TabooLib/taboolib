package taboolib.expansion

open class ContainerBuilder(val name: String) {

    class Data(val name: String, val length: Int, val int: Boolean, val long: Boolean, val double: Boolean, val key: Boolean)

    val dataList = ArrayList<Data>()

    /**
     * 添加数据列
     */
    fun data(name: String, length: Int = 64, int: Boolean = false, long: Boolean = false, double: Boolean = false, key: Boolean = false) {
        dataList += Data(name, length, int, long, double, key)
    }

    class Flatten(name: String) : ContainerBuilder(name) {

        fun key(name: String, length: Int = 64) {
            data(name, length, key = true)
        }

        fun value(name: String, length: Int = 128, int: Boolean = false, long: Boolean = false, double: Boolean = false) {
            data(name, length, int, long, double)
        }

        fun fixed(): Flatten {
            when {
                dataList.size == 0 -> {
                    key("key")
                    value("value")
                }

                dataList.size != 2 -> {
                    error("Invalid container length")
                }
            }
            return this
        }
    }
}