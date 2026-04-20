package taboolib.module.incision.api

/**
 * 方法坐标 — 唯一标识 JVM 中的一个方法。
 *
 * @property owner   类内部名（斜杠形式），如 `org/bukkit/entity/Player`
 * @property name    方法名
 * @property descriptor 方法描述符，如 `(Ljava/lang/String;)V`
 */
data class MethodCoordinate(
    val owner: String,
    val name: String,
    val descriptor: String,
) {

    val ownerClassName: String get() = owner.replace('/', '.')

    val signature: String get() = "$owner.$name$descriptor"

    override fun toString(): String = signature
}
