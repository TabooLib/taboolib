package taboolib.module.nms

/**
 * TabooLib
 * taboolib.module.nms.Test
 *
 * @author sky
 * @since 2021/6/14 8:59 下午
 */
class Test {

    @MinecraftServerProxy(side = ProxySide.NMS, type = ProxyType.CONSTRUCTOR)
    lateinit var packetPlayOutTitle: ProxyCallback<Any>
        private set

    @MinecraftServerProxy(side = ProxySide.NMS, type = ProxyType.ENUM)
    lateinit var enumTitleAction: ProxyEnums
        private set

    @MinecraftServerProxy(side = ProxySide.OBC, type = ProxyType.METHOD, at = "CraftChatMessage")
    lateinit var fromString: ProxyCallback<Array<*>>
        private set

    @MinecraftServerProxy(side = ProxySide.OBC, type = ProxyType.FIELD, at = "CraftPlayer")
    lateinit var resourcePackStatus: ProxyCallback<Enum<*>>
        private set

    @MinecraftServerProxy(side = ProxySide.OBC, type = ProxyType.FIELD, at = "CraftPlayer")
    lateinit var resourcePackHash: ProxyCallback<String>
        private set

    @MinecraftServerProxy(side = ProxySide.OBC, type = ProxyType.METHOD, at = "CraftPlayer")
    lateinit var sendRawMessage: ProxyCallback<Void>
        private set

    @MinecraftServerProxy(side = ProxySide.OBC, type = ProxyType.METHOD, at = "CraftPlayer")
    lateinit var getHandle: ProxyCallback<Any>
        private set

    @MinecraftServerProxy(side = ProxySide.NMS, type = ProxyType.FIELD, at = "EntityPlayer")
    lateinit var playerConnection: ProxyCallback<Any>
        private set

    @MinecraftServerProxy(side = ProxySide.NMS, type = ProxyType.FIELD, at = "PlayerConnection")
    lateinit var networkManager: ProxyCallback<Any>
        private set

    @MinecraftServerProxy(side = ProxySide.NMS, type = ProxyType.METHOD, at = "NetworkManager")
    lateinit var sendPacket: ProxyCallback<Void>
        private set


}