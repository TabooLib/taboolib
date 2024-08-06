package taboolib.expansion

import net.bytebuddy.ByteBuddy
import net.bytebuddy.description.modifier.Visibility
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy
import net.bytebuddy.implementation.FieldAccessor
import net.bytebuddy.implementation.FixedValue
import net.bytebuddy.implementation.MethodCall
import net.bytebuddy.matcher.ElementMatchers
import net.minecraft.server.level.EntityPlayer
import org.bukkit.craftbukkit.v1_17_R1.CraftServer
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.util.unsafeLazy
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.*

class PlayerFakeOpNMSImpl : PlayerFakeOpNMS() {

    @get:Deprecated("internal only, use Player.fakeOp()", level = DeprecationLevel.WARNING)
    @get:ScheduledForRemoval
    val playerFakeOpUtil: PlayerFakeOpUtil by unsafeLazy { PlayerFakeOpUtil() }

    override fun playerFakeOp(player: Player): Player {
        return playerFakeOpUtil.createProxy(player as CraftPlayer)
    }

    @Awake(LifeCycle.ENABLE)
    fun init() = playerFakeOpUtil

    class PlayerFakeOpUtil @Deprecated("internal only", level = DeprecationLevel.WARNING) @ScheduledForRemoval constructor() {

        @get:Deprecated("internal only", level = DeprecationLevel.WARNING)
        @get:ScheduledForRemoval
        val playerFakeOpClass: Class<out CraftPlayer>

        @get:Deprecated("internal only", level = DeprecationLevel.WARNING)
        @get:ScheduledForRemoval
        val playerFakeOpConstructor: Constructor<out CraftPlayer>

        private lateinit var tempCraftPlayer: CraftPlayer

        init {
            // Generate the bytecode of the new class, which extends CraftPlayer
            var dynamicType = ByteBuddy()
                .subclass(CraftPlayer::class.java)
                // Define the field craftPlayer to save the original CraftPlayer
                .defineField("craftPlayer", CraftPlayer::class.java, Visibility.PUBLIC)
                // Define the method hasPermission(String), hasPermission(Permission) and isOp() always returning true
                .method(ElementMatchers.namedOneOf("hasPermission", "isOp"))
                .intercept(FixedValue.value(true))
                // Define the constructor(CraftServer, EntityPlayer, CraftPlayer) to save the original CraftPlayer in the field craftPlayer
                .defineConstructor(Visibility.PUBLIC)
                .withParameters(CraftServer::class.java, EntityPlayer::class.java, CraftPlayer::class.java)
                .intercept(
                    MethodCall.invoke(CraftPlayer::class.java.getDeclaredConstructor(CraftServer::class.java, EntityPlayer::class.java))
                        .withArgument(0, 1)
                        .andThen(FieldAccessor.ofField("craftPlayer").setsArgumentAt(2))
                )

            // Intercept the methods declared by CraftPlayer to the original craftPlayer
            retrieveMethods(CraftPlayer::class.java).forEach { method ->
                dynamicType = dynamicType.defineMethod(method.name, method.returnType, method.modifiers)
                    .withParameters(*method.parameterTypes)
                    .intercept(
                        MethodCall
                            .invoke(method)
                            .onField("craftPlayer")
                            .withAllArguments()
                    )
            }

            // Load the new class by injecting it into the given ClassLoader by reflective access
            playerFakeOpClass = dynamicType.make().load(javaClass.classLoader, ClassLoadingStrategy.Default.INJECTION).loaded
            playerFakeOpConstructor = playerFakeOpClass.getConstructor(CraftServer::class.java, EntityPlayer::class.java, CraftPlayer::class.java)
        }

        @Deprecated("internal only", level = DeprecationLevel.WARNING)
        @ScheduledForRemoval
        fun createProxy(player: CraftPlayer): CraftPlayer {
            tempCraftPlayer = player
            return playerFakeOpConstructor.newInstance(player.server, player.handle, player)
        }

        /**
         * retrieve all methods from the class CraftPlayer，including its private method
         * with all protected or public method from its superclasses or interfaces
         */
        private fun retrieveMethods(clazz: Class<*>): List<Method> {
            val methods = mutableListOf<Method>()
            val visited = mutableSetOf<Class<*>>()
            val queue = ArrayDeque<Class<*>>()

            clazz.let { queue.add(it) }

            while (queue.isNotEmpty()) {
                val currentClass = queue.removeFirst()

                if (visited.add(currentClass) && currentClass != Object::class.java) {

                    currentClass.declaredMethods.forEach { declared ->
                        val isInheritable = Modifier.isProtected(declared.modifiers) || Modifier.isPublic(declared.modifiers)
                        val isNotStatic = !Modifier.isStatic(declared.modifiers)
                        val likes = { methods.filter { like(declared, it) } }

                        if (isInheritable && isNotStatic) {
                            val likeMethods = likes()
                            if (likeMethods.isEmpty()) {
                                methods.add(declared)
                            } else {
                                likeMethods.forEach { likeMethod ->
                                    if (likeMethod.declaringClass.isSuperOf(declared.declaringClass)) {
                                        methods.remove(likeMethod)
                                        methods.add(declared)
                                    }
                                }
                            }
                        }
                    }

                    queue.addAll(currentClass.interfaces)
                    currentClass.superclass?.let { queue.add(it) }
                }
            }

            return methods
        }

        private fun Class<*>.isSuperOf(clazz: Class<*>): Boolean {
            return clazz != this && clazz.isAssignableFrom(this)
        }

        private fun like(a: Method, b: Method): Boolean {
            return a.name == b.name && a.returnType == b.returnType && a.parameterTypes contentEquals b.parameterTypes
        }
    }
}