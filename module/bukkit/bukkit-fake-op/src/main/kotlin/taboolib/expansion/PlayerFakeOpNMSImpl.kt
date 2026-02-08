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
import org.bukkit.entity.Player
import org.bukkit.permissions.Permission
import taboolib.common.util.unsafeLazy
import taboolib.module.nms.nmsClass
import java.lang.reflect.Constructor

class PlayerFakeOpNMSImpl : PlayerFakeOpNMS() {

    val playerFakeOpUtil: PlayerFakeOpUtil by unsafeLazy { PlayerFakeOpUtil() }

    override fun playerFakeOp(player: Player): Player {
        return playerFakeOpUtil.createProxy(player as CraftPlayer)
    }

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
            // Load the new class by injecting it into the given ClassLoader by reflective access
            playerFakeOpClass = dynamicType.load(javaClass.classLoader, ClassLoadingStrategy.Default.INJECTION).loaded
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