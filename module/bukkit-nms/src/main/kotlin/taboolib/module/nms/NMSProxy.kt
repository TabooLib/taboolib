package taboolib.module.nms

/**
 * 由 TabooLib Gradle 插件在打包时封装当前类及其内部类，由运行库按当前服务端版本转译。
 *
 * 使用此注解需要配套的 BukkitNMS 模块、Gradle 插件及运行库，无需开启 enableIsolatedClassloader。
 * 保留原类名，使用当前插件对应的类加载器，可以直接调用构造函数，无需抽象代理类。
 * 不缓存实例或读写转译字节码缓存，Kotlin object 仍遵循自身的单例语义。
 * 类的注解元数据仍参与框架扫描，支持 PlatformSide、Requires、Awake 和事件监听。
 * 不会修复目标版本不存在或签名发生变化的 NMS API。业务代码应从 TabooLib 插件入口访问，
 * 不应从服务端生成的引导主类或其他插件的类加载器直接引用这些类。
 *
 * ```kotlin
 * @NMSProxy
 * class EntityAccess(val entity: org.bukkit.entity.Entity) {
 *     fun handle(): Any = (entity as org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity).handle
 * }
 * ```
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class NMSProxy
