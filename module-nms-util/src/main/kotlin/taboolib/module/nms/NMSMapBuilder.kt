@file:Isolated

package taboolib.module.nms

import org.bukkit.entity.Player
import taboolib.common.Isolated
import taboolib.platform.util.ItemBuilder
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.io.File
import java.net.URL
import javax.imageio.ImageIO

fun buildMap(url: String, width: Int = 128, height: Int = 128, builder: ItemBuilder.() -> Unit = {}): NMSMap {
    return NMSMap(URL(url).openStream().use { ImageIO.read(it) }.zoomed(width, height), builder)
}

fun buildMap(file: File, width: Int = 128, height: Int = 128, builder: ItemBuilder.() -> Unit = {}): NMSMap {
    return NMSMap(ImageIO.read(file).zoomed(width, height), builder)
}

fun buildMap(image: BufferedImage, width: Int = 128, height: Int = 128, builder: ItemBuilder.() -> Unit = {}): NMSMap {
    return NMSMap(image.zoomed(width, height), builder)
}

fun Player.sendMap(url: String, width: Int = 128, height: Int = 128, builder: ItemBuilder.() -> Unit = {}) {
    buildMap(url, width, height, builder).sendTo(this)
}

fun Player.sendMap(file: File, width: Int = 128, height: Int = 128, builder: ItemBuilder.() -> Unit = {}) {
    buildMap(file, width, height, builder).sendTo(this)
}

fun Player.sendMap(image: BufferedImage, width: Int = 128, height: Int = 128, builder: ItemBuilder.() -> Unit = {}) {
    buildMap(image, width, height, builder).sendTo(this)
}

/**
 * 调整图片分辨率
 * 地图最佳显示分辨率为128*128
 */
fun BufferedImage.zoomed(width: Int = 128, height: Int = 128): BufferedImage {
    val wRatio = width * 1.0 / width
    val hRatio = height * 1.0 / height
    val ato = AffineTransformOp(AffineTransform.getScaleInstance(wRatio, hRatio), null)
    return ato.filter(this, null)
}