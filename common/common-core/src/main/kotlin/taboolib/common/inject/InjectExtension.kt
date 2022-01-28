@file:Isolated

package taboolib.common.inject

import taboolib.common.Isolated

inline fun <reified T> inject() {
    return InjectHandler.INSTANCE.inject(T::class.java)
}