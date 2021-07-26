package taboolib.module.kether

import taboolib.common.platform.ProxyPlayer

class PlayerOperator(
    var reader: Reader? = null,
    var writer: Writer? = null,
    val usable: Array<Method> = arrayOf(Method.INCREASE, Method.DECREASE, Method.MODIFY),
) {

    class Reader(val func: (ProxyPlayer) -> Any?)

    class Writer(val func: (ProxyPlayer, Method, Any?) -> Unit)

    enum class Method {

        INCREASE, DECREASE, MODIFY, NONE
    }
}