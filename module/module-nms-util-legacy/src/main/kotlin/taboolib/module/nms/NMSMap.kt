@file:Suppress("DEPRECATION")

package taboolib.module.nms

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.map.MapCanvas
import org.bukkit.map.MapRenderer
import org.bukkit.map.MapView
import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.Reflex.Companion.invokeConstructor
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import org.tabooproject.reflex.Reflex.Companion.setProperty
import org.tabooproject.reflex.Reflex.Companion.unsafeInstance
import taboolib.common.platform.function.submit
import taboolib.common.util.unsafeLazy
import taboolib.library.xseries.XMaterial
import taboolib.platform.util.ItemBuilder
import taboolib.platform.util.buildItem
import taboolib.platform.util.modifyMeta
import java.awt.image.BufferedImage
import java.io.File
import java.lang.reflect.Array
import java.net.URL
import java.util.concurrent.CompletableFuture
import javax.imageio.ImageIO

/**
 * 创建地图画（堵塞）
 *
 * @param url 图像地址
 * @param width 图像宽度
 * @param height 图像高度
 */
@Deprecated("Network I/O on main thread", ReplaceWith("buildMap(URL(url), hand, width, height, builder)", "java.net.URL", "java.util.concurrent.CompletableFuture"))
fun buildMap(
    url: String,
    hand: NMSMap.Hand = NMSMap.Hand.MAIN,
    width: Int = 128,
    height: Int = 128,
    builder: ItemBuilder.() -> Unit = {}
): NMSMap {
    return NMSMap(URL(url).openStream().use { ImageIO.read(it) }.zoomed(width, height), hand, builder)
}

/**
 * 创建地图画（异步）
 *
 * @param url 图像地址
 * @param width 图像宽度
 * @param height 图像高度
 */
@Deprecated("停止维护")
fun buildMap(
    url: URL,
    hand: NMSMap.Hand = NMSMap.Hand.MAIN,
    width: Int = 128,
    height: Int = 128,
    builder: ItemBuilder.() -> Unit = {}
): CompletableFuture<NMSMap> {
    return CompletableFuture.supplyAsync {
        NMSMap(url.openStream().use { ImageIO.read(it) }.zoomed(width, height), hand, builder)
    }
}

/**
 * 创建地图画（堵塞）
 *
 * @param file 图像文件
 * @param width 图像宽度
 * @param height 图像高度
 */
@Deprecated("停止维护")
fun buildMap(
    file: File,
    hand: NMSMap.Hand = NMSMap.Hand.MAIN,
    width: Int = 128,
    height: Int = 128,
    builder: ItemBuilder.() -> Unit = {}
): NMSMap {
    return NMSMap(ImageIO.read(file).zoomed(width, height), hand, builder)
}

/**
 * 创建地图画（堵塞）
 *
 * @param image 图像对象
 * @param width 图像宽度
 * @param height 图像高度
 */
@Deprecated("停止维护")
fun buildMap(
    image: BufferedImage,
    hand: NMSMap.Hand = NMSMap.Hand.MAIN,
    width: Int = 128,
    height: Int = 128,
    builder: ItemBuilder.() -> Unit = {}
): NMSMap {
    return NMSMap(image.zoomed(width, height), hand, builder)
}

/**
 * 打开地图画（异步）
 *
 * @param url 图像地址
 * @param width 图像宽度
 * @param height 图像高度
 */
@Deprecated("停止维护")
fun Player.sendMap(
    url: String,
    hand: NMSMap.Hand = NMSMap.Hand.MAIN,
    width: Int = 128,
    height: Int = 128,
    builder: ItemBuilder.() -> Unit = {}
) {
    buildMap(URL(url), hand, width, height, builder).thenAccept { it.sendTo(this) }
}

/**
 * 打开地图画（异步）
 *
 * @param url 图像地址
 * @param width 图像宽度
 * @param height 图像高度
 */
@Deprecated("停止维护")
fun Player.sendMap(
    url: URL,
    hand: NMSMap.Hand = NMSMap.Hand.MAIN,
    width: Int = 128,
    height: Int = 128,
    builder: ItemBuilder.() -> Unit = {}
) {
    buildMap(url, hand, width, height, builder).thenAccept { it.sendTo(this) }
}

/**
 * 打开地图画（异步）
 *
 * @param file 图像文件
 * @param width 图像宽度
 * @param height 图像高度
 */
@Deprecated("停止维护")
fun Player.sendMap(
    file: File,
    hand: NMSMap.Hand = NMSMap.Hand.MAIN,
    width: Int = 128,
    height: Int = 128,
    builder: ItemBuilder.() -> Unit = {}
) {
    buildMap(file, hand, width, height, builder).sendTo(this)
}

/**
 * 打开地图画（异步）
 *
 * @param image 图像对象
 * @param width 图像宽度
 * @param height 图像高度
 */
@Deprecated("停止维护")
fun Player.sendMap(
    image: BufferedImage,
    hand: NMSMap.Hand = NMSMap.Hand.MAIN,
    width: Int = 128,
    height: Int = 128,
    builder: ItemBuilder.() -> Unit = {}
) {
    buildMap(image, hand, width, height, builder).sendTo(this)
}

/**
 * 调整图片分辨率
 * 地图最佳显示分辨率为128*128
 */
@Deprecated("停止维护")
fun BufferedImage.zoomed(width: Int = 128, height: Int = 128): BufferedImage {
    val tag = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    tag.graphics.drawImage(this, 0, 0, width, height, null)
    return tag
}

/**
 * 地图发包工具
 * 支持 1.8 - 1.17.1
 * @author xbaimiao, sky
 */
class NMSMap(val image: BufferedImage, var hand: Hand = Hand.MAIN, val builder: ItemBuilder.() -> Unit = {}) {

    enum class Hand {

        MAIN, OFF
    }

    companion object {

        val classPacketPlayOutSetSlot = nmsClass("PacketPlayOutSetSlot")
        val classPacketPlayOutMap = nmsClass("PacketPlayOutMap")
        val classCraftItemStack = obcClass("inventory.CraftItemStack")
        val classMapIcon by unsafeLazy { nmsClass("MapIcon") }
        val classMapData: Class<*> by unsafeLazy {
            when {
                MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_20) -> {
                    Class.forName("net.minecraft.world.level.saveddata.maps.WorldMap.b")
                }

                else -> {
                    Class.forName("net.minecraft.world.level.saveddata.maps.WorldMap\$b")
                }
            }
        }
        val classMapId: Class<*> by unsafeLazy { Class.forName("net.minecraft.world.level.saveddata.maps.MapId") }
    }

    val mapRenderer = object : MapRenderer() {

        var rendered = false

        override fun render(mapView: MapView, mapCanvas: MapCanvas, player: Player) {
            if (rendered) {
                return
            }
            mapCanvas.drawImage(0, 0, image)
            rendered = true
        }
    }

    val mapView by unsafeLazy {
        val mapView = Bukkit.createMap(Bukkit.getWorlds()[0])
        mapView.addRenderer(mapRenderer)
        mapView
    }

    val mapItem by unsafeLazy {
        val map = if (MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_13)) {
            buildItem(XMaterial.FILLED_MAP, builder)
        } else {
            buildItem(XMaterial.FILLED_MAP) {
                damage = mapView.invokeMethod<Short>("getId")!!.toInt()
                builder(this)
            }
        }
        if (MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_13)) {
            map.modifyMeta<MapMeta> { mapView = this@NMSMap.mapView }
        } else {
            map
        }
    }

    fun sendTo(player: Player) {
        submit(delay = 3) {
            val container = if (MinecraftVersion.isUniversal) {
                player.getProperty<Any>("entity/inventoryMenu")
            } else {
                player.getProperty<Any>("entity/defaultContainer")
            }!!
            val windowsId = if (MinecraftVersion.isUniversal) {
                container.getProperty<Int>("containerId")
            } else {
                container.getProperty<Int>("windowId")
            }!!
            val nmsItem = classCraftItemStack.invokeMethod<Any>("asNMSCopy", mapItem, isStatic = true)
            player.sendPacket(classPacketPlayOutSetSlot.unsafeInstance().also {
                if (MinecraftVersion.isUniversal) {
                    it.setProperty("containerId", windowsId)
                    it.setProperty("stateId", 1)
                    it.setProperty("slot", getMainHandSlot(player))
                    it.setProperty("itemStack", nmsItem)
                } else {
                    it.setProperty("a", windowsId)
                    it.setProperty("b", getMainHandSlot(player))
                    it.setProperty("c", nmsItem)
                }
            })
            val buffer = mapView.invokeMethod<Any>("render", player)!!.getProperty<ByteArray>("buffer")
            val packet = classPacketPlayOutMap.unsafeInstance()
            when {
                // 1.21+
                MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_21) -> {
                    packet.setProperty("mapId", classMapId.invokeConstructor((mapItem.itemMeta as MapMeta).mapId))
                    packet.setProperty("scale", mapView.scale.value)
                    packet.setProperty("locked", false)
                    packet.setProperty("decorations", ArrayList<Any>())
                    packet.setProperty("colorPatch", classMapData.unsafeInstance().also {
                        it.setProperty("startX", 0)
                        it.setProperty("startY", 0)
                        it.setProperty("width", 128)
                        it.setProperty("height", 128)
                        it.setProperty("mapColors", buffer)
                    })
                }
                // 1.20+
                MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_20) -> {
                    packet.setProperty("mapId", mapView.id)
                    packet.setProperty("scale", mapView.scale.value)
                    packet.setProperty("locked", false)
                    packet.setProperty("decorations", ArrayList<Any>())
                    packet.setProperty("colorPatch", classMapData.unsafeInstance().also {
                        it.setProperty("startX", 0)
                        it.setProperty("startY", 0)
                        it.setProperty("width", 128)
                        it.setProperty("height", 128)
                        it.setProperty("mapColors", buffer)
                    })
                }
                // 1.17+
                MinecraftVersion.isUniversal -> {
                    packet.setProperty("mapId", (mapItem.itemMeta as MapMeta).mapId)
                    packet.setProperty("scale", mapView.scale.value)
                    packet.setProperty("locked", false)
                    packet.setProperty("decorations", ArrayList<Any>())
                    packet.setProperty("colorPatch", classMapData.unsafeInstance().also {
                        it.setProperty("startX", 0)
                        it.setProperty("startY", 0)
                        it.setProperty("width", 128)
                        it.setProperty("height", 128)
                        it.setProperty("mapColors", buffer)
                    })
                }
                // 1.14+
                MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_14) -> {
                    packet.setProperty("a", (mapItem.itemMeta as MapMeta).mapId)
                    packet.setProperty("b", mapView.scale.value)
                    packet.setProperty("c", false)
                    packet.setProperty("d", false)
                    packet.setProperty("e", Array.newInstance(classMapIcon, 0))
                    packet.setProperty("f", 0)
                    packet.setProperty("g", 0)
                    packet.setProperty("h", 128)
                    packet.setProperty("i", 128)
                    packet.setProperty("j", buffer)
                }
                // 1.12+
                MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_12) -> {
                    if (MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_13)) {
                        packet.setProperty("a", (mapItem.itemMeta as MapMeta).mapId)
                    } else {
                        packet.setProperty("a", mapView.invokeMethod<Short>("getId")!!.toInt())
                    }
                    packet.setProperty("b", mapView.scale.value)
                    packet.setProperty("c", false)
                    packet.setProperty("d", Array.newInstance(classMapIcon, 0))
                    packet.setProperty("e", 0)
                    packet.setProperty("f", 0)
                    packet.setProperty("g", 128)
                    packet.setProperty("h", 128)
                    packet.setProperty("i", buffer)
                }
                // 1.12-
                else -> {
                    packet.setProperty("a", mapView.id)
                    packet.setProperty("b", mapView.scale.value)
                    packet.setProperty("c", Array.newInstance(classMapIcon, 0))
                    packet.setProperty("d", 0)
                    packet.setProperty("e", 0)
                    packet.setProperty("f", 128)
                    packet.setProperty("g", 128)
                    packet.setProperty("h", buffer)
                }
            }
            player.sendPacket(packet)
        }
    }

    private fun getMainHandSlot(player: Player): Int {
        if (hand == Hand.OFF) {
            return 45
        }
        return player.inventory.heldItemSlot + 36
    }
}
