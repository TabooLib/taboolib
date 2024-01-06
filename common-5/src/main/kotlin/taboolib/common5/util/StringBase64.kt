@file:Isolated
@file:Suppress("NOTHING_TO_INLINE")

package taboolib.common5.util

import taboolib.common.Isolated
import java.nio.charset.StandardCharsets
import java.util.*

inline fun ByteArray.encodeBase64(): String {
    return Base64.getEncoder().encode(this).toString(StandardCharsets.UTF_8)
}

inline fun String.encodeBase64(): String {
    return Base64.getEncoder().encode(toByteArray()).toString(StandardCharsets.UTF_8)
}

inline fun ByteArray.decodeBase64(): ByteArray {
    return Base64.getDecoder().decode(this)
}

inline fun String.decodeBase64(): ByteArray {
    return Base64.getDecoder().decode(this)
}