package taboolib.common.util

inline fun <T> T?.ifNull(action: () -> Unit): T? {
    if (this == null) action()
    return this
}