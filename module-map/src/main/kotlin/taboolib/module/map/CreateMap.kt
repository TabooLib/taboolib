package taboolib.module.map

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.map.MapCanvas
import org.bukkit.map.MapRenderer
import org.bukkit.map.MapView
import taboolib.library.xseries.XMaterial
import taboolib.platform.BukkitPlugin
import taboolib.platform.util.ItemBuilder
import java.awt.image.BufferedImage

/**
 * 地图发包工具
 * 支持1.12.2 - 1.17.1
 * @author xbaimiao
 */
class CreateMap(val image: BufferedImage, val name: String?, val lore: List<String>?) {

    companion object {

        private var packetPlayOutMapClass: Class<*>? = null
        private var packetPlayOutSetSlotClass: Class<*>? = null
        private var itemStackClass: Class<*>? = null
        private var entityHumanClass: Class<*>? = null
        private var containerClass: Class<*>? = null
        private var entityPlayerClass: Class<*>? = null
        private var playerConnectionClass: Class<*>? = null
        private var packetClass: Class<*>? = null
        private var bClass: Class<*>? = null

        init {
            if (Version.isBefore(Version.v1_17)) {
                packetPlayOutMapClass = Version.getNmsClass("PacketPlayOutMap")
                packetPlayOutSetSlotClass = Version.getNmsClass("PacketPlayOutSetSlot")
                itemStackClass = Version.getNmsClass("ItemStack")
                entityHumanClass = Version.getNmsClass("EntityHuman")
                containerClass = Version.getNmsClass("Container")
                entityPlayerClass = Version.getNmsClass("EntityPlayer")
                playerConnectionClass = Version.getNmsClass("PlayerConnection")
                packetClass = Version.getNmsClass("Packet")
            } else {
                packetPlayOutMapClass = Version.getClass("net.minecraft.network.protocol.game.PacketPlayOutMap")
                packetPlayOutSetSlotClass = Version.getClass("net.minecraft.network.protocol.game.PacketPlayOutSetSlot")
                itemStackClass = Version.getClass("net.minecraft.world.item.ItemStack")
                entityHumanClass = Version.getClass("net.minecraft.world.entity.player.EntityHuman")
                containerClass = Version.getClass("net.minecraft.world.inventory.Container")
                entityPlayerClass = Version.getClass("net.minecraft.server.level.EntityPlayer")
                playerConnectionClass = Version.getClass("net.minecraft.server.network.PlayerConnection")
                packetClass = Version.getClass("net.minecraft.network.protocol.Packet")
                bClass = Version.getClass("net.minecraft.world.level.saveddata.maps.WorldMap\$b")
            }
        }

        private val renderDataClass = Version.getObcClass("map.RenderData")
        private val craftPlayerClass = Version.getObcClass("entity.CraftPlayer")
        private val craftItemStackClass = Version.getObcClass("inventory.CraftItemStack")
        private val craftMapViewClass = Version.getObcClass("map.CraftMapView")
        private val mapViewClass = Class.forName("org.bukkit.map.MapView")

    }

    private val renderer = object : MapRenderer() {
        var rendered = false
        override fun render(mapView: MapView, mapCanvas: MapCanvas, player: Player) {
            if (rendered)
                return
            mapCanvas.drawImage(0, 0, image)
            rendered = true
        }
    }

    private fun v1122MapId(): Short {
        val id = mapViewClass.getDeclaredMethod("getId").invoke(mapView)
        return (id as Number).toShort()
    }

    private lateinit var mapMeta: MapMeta

    private val mapView: MapView by lazy {
        val mapView = Bukkit.createMap(Bukkit.getWorlds()[0])
        mapView.addRenderer(renderer)
        mapView
    }

    /**
     * 设置显示名称
     */
    fun setDisplayName(name: String?) {
        val meta = mapItem.itemMeta ?: return
        meta.setDisplayName(name)
        mapItem.itemMeta = meta
    }

    /**
     * 设置显示lore
     */
    fun setLore(lore: List<String>) {
        val meta = mapItem.itemMeta ?: return
        meta.lore = lore
        mapItem.itemMeta = meta
    }

    /**
     * 实体物品
     */
    val mapItem by lazy {
        val material =
            XMaterial.matchXMaterial(Material.getMaterial(if (Version.isAfter(Version.v1_13)) "FILLED_MAP" else "MAP")!!)

        val map = if (Version.isAfter(Version.v1_13)) {
            ItemBuilder(material).let {
                it.name = name
                lore?.let { it1 -> it.lore.addAll(it1) }
                it.build()
            }
        } else {
            ItemBuilder(ItemStack(material.parseMaterial()!!, 1, v1122MapId())).let {
                it.name = name
                lore?.let { it1 -> it.lore.addAll(it1) }
                it.build()
            }
        }

        mapMeta = map.itemMeta as MapMeta
        if (Version.isAfter(Version.v1_13)) {
            mapMeta.mapView = mapView //1.12.2不能用
        }
        map.itemMeta = mapMeta
        map
    }

    private fun sendPacket(player: Player, packet: Any) {
        val employer: Any = craftPlayerClass.getDeclaredMethod("getHandle").invoke(player)
        val con = if (Version.isBefore(Version.v1_17)) "playerConnection" else "b"
        val playerConnection = entityPlayerClass!!.getDeclaredField(con).get(employer)
        playerConnectionClass!!.getDeclaredMethod("sendPacket", packetClass).invoke(playerConnection, packet)
    }

    fun sendMap(player: Player) {
        Bukkit.getScheduler().runTaskLater(BukkitPlugin.getInstance(), Runnable {
            val employer: Any = craftPlayerClass.getDeclaredMethod("getHandle").invoke(player)
            val container = if (Version.isBefore(Version.v1_17)) "defaultContainer" else "bU"
            val defaultContainer = entityHumanClass!!.getDeclaredField(container).get(employer)
            val id = if (Version.isBefore(Version.v1_17)) "windowId" else "j"
            val windowId = containerClass!!.getDeclaredField(id).get(defaultContainer)
            val emItemStack =
                craftItemStackClass.getDeclaredMethod("asNMSCopy", ItemStack::class.java).invoke(null, mapItem)
            val itemPacket = packetPlayOutSetSlotClass!!
                .getConstructor(Int::class.java, Int::class.java, Int::class.java, itemStackClass)
                .newInstance(windowId, 1, getMainHandSlot(player), emItemStack)
            sendPacket(player, itemPacket)

            val buffer = renderDataClass.getDeclaredField("buffer")
                .get(craftMapViewClass.getDeclaredMethod("render", craftPlayerClass).invoke(mapView, player))
            if (Version.isAfter(Version.v1_17)) {
                val mapPacket = packetPlayOutMapClass!!.getConstructor(
                    Int::class.java,
                    Byte::class.java,
                    Boolean::class.java,
                    Collection::class.java,
                    bClass
                ).newInstance(
                    mapMeta.mapId,
                    mapView.scale.value,
                    false,
                    ArrayList<Any>(),
                    bClass!!.getConstructor(
                        Int::class.java,
                        Int::class.java,
                        Int::class.java,
                        Int::class.java,
                        ByteArray::class.java
                    ).newInstance(
                        0, 0, 128, 128, buffer
                    )
                )
                sendPacket(player, mapPacket)
                return@Runnable
            }
            if (Version.isAfter(Version.v1_14)) {
                val mapPacket = packetPlayOutMapClass!!
                    .getConstructor(
                        Int::class.java,
                        Byte::class.java,
                        Boolean::class.java,
                        Boolean::class.java,
                        Collection::class.java,
                        ByteArray::class.java,
                        Int::class.java,
                        Int::class.java,
                        Int::class.java,
                        Int::class.java
                    ).newInstance(
                        mapMeta.mapId,
                        mapView.scale.value,
                        false, false,
                        ArrayList<Any>(),
                        buffer,
                        0, 0, 128, 128
                    )
                sendPacket(player, mapPacket)
                return@Runnable
            }
            if (Version.isAfter(Version.v1_12)) {
                val mapPacket = packetPlayOutMapClass!!
                    .getConstructor(
                        Int::class.java,
                        Byte::class.java,
                        Boolean::class.java,
                        Collection::class.java,
                        ByteArray::class.java,
                        Int::class.java,
                        Int::class.java,
                        Int::class.java,
                        Int::class.java
                    )
                val packet: Any
                if (Version.isAfter(Version.v1_13)) {
                    packet = mapPacket.newInstance(
                        mapMeta.mapId,
                        mapView.scale.value,
                        false,
                        ArrayList<Any>(),
                        buffer,
                        0, 0, 128, 128
                    )
                } else {
                    packet = mapPacket.newInstance(
                        v1122MapId().toInt(),
                        mapView.scale.value,
                        false,
                        ArrayList<Any>(),
                        buffer,
                        0, 0, 128, 128
                    )
                }
                sendPacket(player, packet)
                return@Runnable
            }
        }, 3L)
    }

    private fun getMainHandSlot(player: Player): Int {
        return player.inventory.heldItemSlot + 36
    }

}