package taboolib.module.nms

/** 由 NMSProxy 加载入口调用；资源中的原始字节码尚未经过 Paper 的插件重映射。 */
object NMSProxyRuntime {

    @JvmStatic
    fun transform(name: String, bytes: ByteArray): ByteArray {
        return AsmClassTranslation(name).translate(bytes, true)
    }
}
