package taboolib.module.incision.api

/**
 * 目标解析快照 — 在 Checkup 阶段产生，记录扫描期对一个切术的解析结果。
 */
data class Anatomy(
    val incisionId: String,
    val rawDescriptor: String,
    val translatedDescriptor: String?,
    val coordinate: MethodCoordinate?,
    val resolverName: String,
    val resolverEnv: String,
    val ok: Boolean,
    val reason: String? = null,
)
