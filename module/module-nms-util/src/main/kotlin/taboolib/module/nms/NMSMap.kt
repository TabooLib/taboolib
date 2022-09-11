package taboolib.module.nms

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.map.MapCanvas
import org.bukkit.map.MapRenderer
import org.bukkit.map.MapView
import taboolib.common.Isolated
import taboolib.common.platform.function.submit
import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import org.tabooproject.reflex.Reflex.Companion.setProperty
import org.tabooproject.reflex.Reflex.Companion.unsafeInstance
import taboolib.library.xseries.XMaterial
import taboolib.platform.util.ItemBuilder
import taboolib.platform.util.buildItem
import taboolib.platform.util.modifyMeta
import java.awt.image.BufferedImage
import java.lang.reflect.Array

/**
 * 地图发包工具
 * 支持1.8 - 1.17.1
 * @author xbaimiao, sky
 */
@Isolated
class NMSMap(val image: BufferedImage, val hand: Hand = Hand.MAIN, val builder: ItemBuilder.() -> Unit = {}) {

    enum class Hand {
        MAIN, OFF
    }

    companion object {

        val classPacketPlayOutSetSlot = nmsClass("PacketPlayOutSetSlot")
        val classPacketPlayOutMap = nmsClass("PacketPlayOutMap")
        val classCraftItemStack = obcClass("inventory.CraftItemStack")
        val classMapIcon by lazy { nmsClass("MapIcon") }
        val classMapData: Class<*> by lazy { Class.forName("net.minecraft.world.level.saveddata.maps.WorldMap\$b") }
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

    val mapView by lazy {
        val mapView = Bukkit.createMap(Bukkit.getWorlds()[0])
        mapView.addRenderer(mapRenderer)
        mapView
    }

    val mapItem by lazy {
        val map = if (MinecraftVersion.major >= 5) {
            buildItem(XMaterial.FILLED_MAP, builder)
        } else {
            buildItem(XMaterial.FILLED_MAP) {
                damage = mapView.invokeMethod<Short>("getId")!!.toInt()
                builder(this)
            }
        }
        if (MinecraftVersion.major >= 5) {
            map.modifyMeta<MapMeta> {
                mapView = this@NMSMap.mapView
            }
        } else {
            map
        }
    }

    fun sendTo(player: Player) {
        submit(delay = 3, async = true) {
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
                MinecraftVersion.major >= 6 -> {
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
                MinecraftVersion.major >= 4 -> {
                    if (MinecraftVersion.major >= 5) {
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