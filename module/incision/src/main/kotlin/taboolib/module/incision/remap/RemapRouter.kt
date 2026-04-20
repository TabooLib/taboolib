package taboolib.module.incision.remap

/**
 * 路由器 — 根据 owner 决定走 [NoopResolver] 还是 NMS resolver。
 *
 * Bootstrap 阶段若检测到 `taboolib.module.nms.MinecraftVersion` 在 classpath，
 * 会把 [nms] 字段设为 `TabooLibNmsResolver`，否则保留为 null 并一律走 Noop。
 */
object RemapRouter : NameResolver {

    @Volatile
    var nms: NameResolver? = null

    private fun pick(owner: String): NameResolver {
        val r = nms
        if (r != null && r.isRemappableTarget(owner)) return r
        return NoopResolver
    }

    override fun resolveOwner(internalName: String): String = pick(internalName).resolveOwner(internalName)

    override fun resolveMethod(owner: String, name: String, desc: String): Pair<String, String> =
        pick(owner).resolveMethod(owner, name, desc)

    override fun resolveField(owner: String, name: String, desc: String): Pair<String, String> =
        pick(owner).resolveField(owner, name, desc)

    override fun isRemappableTarget(owner: String): Boolean = nms?.isRemappableTarget(owner) == true

    override fun envSignature(): String = "router:${nms?.envSignature() ?: "noop-only"}"

    override fun name(): String = "RemapRouter[${nms?.name() ?: "noop"}]"
}
