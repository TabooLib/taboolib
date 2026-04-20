package taboolib.module.incision.runtime

import java.util.Base64

/**
 * dispatch 签名协议编解码。
 *
 * 约定格式：
 * - phase 入口：`baseSig@PHASE`
 * - site 入口：`baseSig#encodedAdviceId`
 * - phase + site 不叠加；site 侧用 adviceId 精确路由到单条 entry
 *
 * adviceId 本身可能含 `@` / `#`，因此统一做 URL-safe Base64 编码并加前缀，
 * 避免被 phase / id 分隔逻辑误拆。
 */
object DispatchSignatureCodec {

    private const val advicePrefix = "b64:"
    private val phaseSuffixes = setOf("LEAD", "TRAIL", "SPLICE", "TRAIL_THROW")

    data class Parsed(
        val baseSig: String,
        val adviceId: String?,
        val phase: String?,
    )

    fun compose(baseSig: String, adviceId: String): String {
        if (adviceId.isBlank()) return baseSig
        return "$baseSig#${encodeAdviceId(adviceId)}"
    }

    fun parse(targetSig: String): Parsed {
        val (sigWithoutPhase, phase) = splitPhase(targetSig)
        val hashIdx = sigWithoutPhase.indexOf('#')
        if (hashIdx < 0) return Parsed(sigWithoutPhase, null, phase)
        val baseSig = sigWithoutPhase.substring(0, hashIdx)
        val encodedAdviceId = sigWithoutPhase.substring(hashIdx + 1).takeIf { it.isNotEmpty() }
        return Parsed(baseSig, encodedAdviceId?.let(::decodeAdviceId), phase)
    }

    private fun splitPhase(targetSig: String): Pair<String, String?> {
        val atIdx = targetSig.lastIndexOf('@')
        if (atIdx <= 0) return targetSig to null
        val maybePhase = targetSig.substring(atIdx + 1)
        return if (maybePhase in phaseSuffixes) {
            targetSig.substring(0, atIdx) to maybePhase
        } else {
            targetSig to null
        }
    }

    private fun encodeAdviceId(adviceId: String): String {
        val encoded = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(adviceId.toByteArray(Charsets.UTF_8))
        return advicePrefix + encoded
    }

    private fun decodeAdviceId(encodedAdviceId: String): String {
        if (!encodedAdviceId.startsWith(advicePrefix)) return encodedAdviceId
        val encoded = encodedAdviceId.removePrefix(advicePrefix)
        return String(Base64.getUrlDecoder().decode(encoded), Charsets.UTF_8)
    }
}
