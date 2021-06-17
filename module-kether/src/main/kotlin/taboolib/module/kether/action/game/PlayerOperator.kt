package taboolib.module.kether.action.game

import taboolib.common.platform.ProxyPlayer

class PlayerOperator(
    val read: ((ProxyPlayer) -> Any?)? = { },
    val write: ((ProxyPlayer, Symbol, Any?) -> Unit)? = { _, _, _ -> }
)