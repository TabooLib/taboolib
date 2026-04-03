package taboolib.expansion

import net.bytebuddy.ByteBuddy
import net.bytebuddy.description.modifier.Visibility
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy
import net.bytebuddy.implementation.FieldAccessor
import net.bytebuddy.implementation.FixedValue
import net.bytebuddy.implementation.MethodCall
import net.bytebuddy.implementation.MethodDelegation
import net.bytebuddy.implementation.bind.annotation.FieldValue
import net.bytebuddy.implementation.bind.annotation.Pipe
import net.bytebuddy.implementation.bind.annotation.RuntimeType
import net.bytebuddy.matcher.ElementMatchers
import net.minecraft.server.level.EntityPlayer
import org.bukkit.craftbukkit.v1_17_R1.CraftServer
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.permissions.Permission
import taboolib.common.util.unsafeLazy
import taboolib.module.nms.nmsClass
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.lang.reflect.Proxy

class PlayerFakeOpNMSImpl : PlayerFakeOpNMS() {

    val playerFakeOpUtil: PlayerFakeOpUtil by unsafeLazy { PlayerFakeOpUtil() }

    override fun playerFakeOp(player: Player): Player {
        return playerFakeOpUtil.createProxy(player as CraftPlayer)
    }

    override fun dispatchCommandAsOp(player: Player, command: String): Boolean {
        val craftPlayer = player as CraftPlayer
        // 尝试 NMS 派发（1.13+ Brigadier 环境）
        val h = getOrCreateDispatchHelper(craftPlayer)
        if (h != null) {
            val entityPlayer = craftPlayer.handle
            val fakeOpPlayer = playerFakeOpUtil.createProxy(craftPlayer)
            // CommandSourceStack + ALL_PERMISSIONS（NMS 层权限）
            val css = h.createCommandSourceStack.invoke(entityPlayer)
            val opCss = h.withPermission.invoke(css, h.permissionArg)
            // 替换 CommandSource，使 getBukkitSender() 返回 FakeOp 代理（Bukkit 层权限）
            val proxySource = Proxy.newProxyInstance(h.commandSourceClass.classLoader, arrayOf(h.commandSourceClass)) { _, method, args ->
                if (method.name == "getBukkitSender") fakeOpPlayer else method.invoke(entityPlayer, *(args ?: emptyArray()))
            }
            val finalCss = h.withSource.invoke(opCss, proxySource)
            // NMS 同步派发
            val commands = h.getCommands.invoke(h.getServer.invoke(craftPlayer.server))
            h.dispatchCommand.invoke(commands, finalCss, command)
            return true
        }
        // 回退：无 Brigadier 的旧版本（1.12 及以下），直接用 FakeOp 代理派发
        return Bukkit.dispatchCommand(playerFakeOpUtil.createProxy(craftPlayer), command)
    }

    private fun getOrCreateDispatchHelper(craftPlayer: CraftPlayer): DispatchHelper? {
        val cached = dispatchHelper
        if (cached === UNAVAILABLE) return null
        if (cached is DispatchHelper) return cached
        return try {
            DispatchHelper.create(craftPlayer.handle, craftPlayer.server as CraftServer).also { dispatchHelper = it }
        } catch (e: Throwable) {
            e.printStackTrace()
            dispatchHelper = UNAVAILABLE
            null
        }
    }

    companion object {
        private object UNINITIALIZED
        private object UNAVAILABLE

        @Volatile
        private var dispatchHelper: Any? = UNINITIALIZED
    }

    /**
     * 缓存 NMS 命令派发所需的反射引用。
     * 方法名优先 + 签名回退，兼容 Spigot 1.13+ / Paper 1.20.6+ / Paper 1.21.8+。
     */
    class DispatchHelper(
        val createCommandSourceStack: Method,
        val withPermission: Method,
        val permissionArg: Any,
        val withSource: Method,
        val commandSourceClass: Class<*>,
        val getServer: Method,
        val getCommands: Method,
        val dispatchCommand: Method,
    ) {
        companion object {

            fun create(entityPlayer: Any, craftServer: CraftServer): DispatchHelper {
                val epClass = entityPlayer.javaClass
                val createCSS = find(epClass, "createCommandSourceStack") { it.parameterCount == 0 && !it.returnType.isPrimitive }
                val cssClass = createCSS.returnType

                // withPermission：int（1.13~1.21.7）或 PermissionSet（1.21.8+）
                val withPermInt = cssClass.methods.firstOrNull { m ->
                    m.name == "withPermission" && m.parameterCount == 1 && m.parameterTypes[0] == Integer.TYPE && m.returnType == cssClass
                }
                val withPerm: Method
                val permArg: Any
                if (withPermInt != null) {
                    withPerm = withPermInt
                    permArg = 4
                } else {
                    withPerm = cssClass.methods.first { m ->
                        m.name == "withPermission" && m.parameterCount == 1 && m.returnType == cssClass
                    }
                    permArg = withPerm.parameterTypes[0].getField("ALL_PERMISSIONS").get(null)!!
                }

                // withSource(CommandSource)
                val withSource = find(cssClass, "withSource") { it.parameterCount == 1 && it.returnType == cssClass && it.parameterTypes[0].isInterface }

                // CraftServer.getServer() → MinecraftServer
                val getServer = craftServer.javaClass.getMethod("getServer")
                val mcServer = getServer.invoke(craftServer)!!

                // getCommands()
                val getCommands = find(mcServer.javaClass, "getCommands", "vanillaCommandDispatcher") { it.parameterCount == 0 && !it.returnType.isPrimitive }

                // performPrefixedCommand / performCommand
                val dispatch = find(getCommands.returnType, "performPrefixedCommand", "performCommand") {
                    it.parameterCount == 2 && it.parameterTypes[0] == cssClass && it.parameterTypes[1] == String::class.java
                }

                return DispatchHelper(createCSS, withPerm, permArg, withSource, withSource.parameterTypes[0], getServer, getCommands, dispatch)
            }

            /** 按名称优先查找，回退到签名匹配 */
            private fun find(clazz: Class<*>, vararg names: String, predicate: (Method) -> Boolean): Method {
                for (name in names) {
                    clazz.methods.firstOrNull { it.name == name && predicate(it) }?.let { return it }
                }
                return clazz.methods.firstOrNull(predicate)
                    ?: error("Cannot find method [${names.joinToString()}] in ${clazz.name}")
            }
        }
    }

    // ============================================================
    // FakeOp CraftPlayer 代理（ByteBuddy 生成）
    // ============================================================

    class PlayerFakeOpUtil {

        val playerFakeOpClass: Class<out CraftPlayer>
        val playerFakeOpConstructor: Constructor<out CraftPlayer>

        private lateinit var tempCraftPlayer: CraftPlayer

        init {
            val entityPlayerClass = nmsClass("EntityPlayer")
            // Generate the bytecode of the new class, which extends CraftPlayer
            val dynamicType = ByteBuddy()
                .subclass(CraftPlayer::class.java)
                // Define the field craftPlayer to save the original CraftPlayer
                .defineField("craftPlayer", CraftPlayer::class.java, Visibility.PUBLIC)
                // Define the method hasPermission(String) always returning true
                .defineMethod("hasPermission", Boolean::class.java, Visibility.PUBLIC)
                .withParameter(String::class.java)
                .intercept(FixedValue.value(true))
                // Define the method hasPermission(Permission) always returning true
                .defineMethod("hasPermission", Boolean::class.java, Visibility.PUBLIC)
                .withParameter(Permission::class.java)
                .intercept(FixedValue.value(true))
                // Define the constructor(CraftServer, EntityPlayer, CraftPlayer) to save the original CraftPlayer in the field craftPlayer
                .defineConstructor(Visibility.PUBLIC)
                .withParameters(CraftServer::class.java, entityPlayerClass, CraftPlayer::class.java)
                .intercept(
                    MethodCall.invoke(CraftPlayer::class.java.getDeclaredConstructor(CraftServer::class.java,entityPlayerClass))
                        .withArgument(0, 1)
                        .andThen(FieldAccessor.ofField("craftPlayer").setsArgumentAt(2))
                )
                // Intercept the methods declared by CraftPlayer to the original craftPlayer
                .method(ElementMatchers.isDeclaredBy(CraftPlayer::class.java))
                .intercept(
                    MethodDelegation.withDefaultConfiguration()
                        .withBinders(Pipe.Binder.install(Forwarder::class.java))
                        .to(this)
                )
                // Intercept the method isOp() to make it always return true
                .method(ElementMatchers.named("isOp"))
                .intercept(FixedValue.value(true))
                .make()
            // CHILD_FIRST: avoid Java 16+ module restrictions (INJECTION) and AsmClassLoader interference (WRAPPER)
            playerFakeOpClass = dynamicType.load(javaClass.classLoader, ClassLoadingStrategy.Default.CHILD_FIRST).loaded
            playerFakeOpConstructor = playerFakeOpClass.getConstructor(CraftServer::class.java, entityPlayerClass, CraftPlayer::class.java)
        }

        @RuntimeType
        fun whatever(@Pipe pipe: Forwarder<Any, CraftPlayer>, @FieldValue("craftPlayer") craftPlayer: CraftPlayer?): Any {
            // When the object is being initialized, its field craftPlayer will be null, so using tempCraftPlayer instead
            return pipe.to(craftPlayer ?: tempCraftPlayer)
        }

        fun createProxy(player: CraftPlayer): CraftPlayer {
            tempCraftPlayer = player
            return playerFakeOpConstructor.newInstance(player.server, player.handle, player)
        }

        interface Forwarder<T, S> {
            fun to(target: S): T
        }
    }
}
