package taboolib.module.incision.remap

/**
 * 默认空实现 — 任何名字原样返回，用于非 NMS 场景。
 */
object NoopResolver : NameResolver {

    override fun resolveOwner(internalName: String): String = internalName

    override fun resolveMethod(owner: String, name: String, desc: String): Pair<String, String> = name to desc

    override fun resolveField(owner: String, name: String, desc: String): Pair<String, String> = name to desc

    override fun isRemappableTarget(owner: String): Boolean = false

    override fun envSignature(): String = "noop"
}
