@file:Isolated

package taboolib.module.nms

import org.bukkit.entity.Player
import taboolib.common.Isolated
import taboolib.platform.util.ItemBuilder
import java.awt.image.BufferedImage
import java.io.File
import java.net.URL
import javax.imageio.ImageIO

fun buildMap(
    url: String,
    hand: NMSMap.Hand = NMSMap.Hand.MAIN,
    width: Int = 128,
    height: Int = 128,
    builder: ItemBuilder.() -> Unit = {}
): NMSMap {
    return NMSMap(URL(url).openStream().use { ImageIO.read(it) }.zoomed(width, height), hand, builder)
}

fun buildMap(
    file: File,
    hand: NMSMap.Hand = NMSMap.Hand.MAIN,
    width: Int = 128,
    height: Int = 128,
    builder: ItemBuilder.() -> Unit = {}
): NMSMap {
    return NMSMap(ImageIO.read(file).zoomed(width, height), hand, builder)
}

fun buildMap(
    image: BufferedImage,
    hand: NMSMap.Hand = NMSMap.Hand.MAIN,
    width: Int = 128,
    height: Int = 128,
    builder: ItemBuilder.() -> Unit = {}
): NMSMap {
    return NMSMap(image.zoomed(width, height), hand, builder)
}

fun Player.sendMap(
    url: String,
    hand: NMSMap.Hand = NMSMap.Hand.MAIN,
    width: Int = 128,
    height: Int = 128,
    builder: ItemBuilder.() -> Unit = {}
) {
    buildMap(url, hand, width, height, builder).sendTo(this)
}

fun Player.sendMap(
    file: File,
    hand: NMSMap.Hand = NMSMap.Hand.MAIN,
    width: Int = 128,
    height: Int = 128,
    builder: ItemBuilder.() -> Unit = {}
) {
    buildMap(file, hand, width, height, builder).sendTo(this)
}

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
fun BufferedImage.zoomed(width: Int = 128, height: Int = 128): BufferedImage {
    val tag = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    tag.graphics.drawImage(this, 0, 0, width, height, null)
    return tag
}