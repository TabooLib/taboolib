package taboolib.module.incision.remap

/**
 * 名称解析器 — 把用户写的目标名（可能是 Mojang 名、Spigot 名或版本化包名）
 * 转换为当前运行环境下真实存在的类/方法/字段名。
 *
 * - 默认实现 [NoopResolver] 对所有名字原样返回。
 * - 当 `module:bukkit-nms` 存在时，Bootstrap 会绑定 TabooLibNmsResolver
 *   并对 `net/minecraft/`、`com/mojang/`、`org/bukkit/craftbukkit/` 等 owner
 *   自动走 NMS 映射路径。
 */
interface NameResolver {

    /** 把 internalName (斜杠形式) 规范化为当前运行环境下的 internalName */
    fun resolveOwner(internalName: String): String

    /** 返回 (规范化名, 规范化描述符) */
    fun resolveMethod(owner: String, name: String, desc: String): Pair<String, String>

    /** 返回 (规范化名, 规范化描述符) */
    fun resolveField(owner: String, name: String, desc: String): Pair<String, String>

    /** 是否需要重映射（Noop 对非 NMS 返回 false） */
    fun isRemappableTarget(owner: String): Boolean

    /** 进入缓存键的环境签名；环境变化时强制重译 */
    fun envSignature(): String

    /** 诊断用名称 */
    fun name(): String = javaClass.simpleName
}
