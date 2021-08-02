package taboolib.module.map

import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Image工具
 * @author xbaimiao
 */
object ImageUtils {

    /**
     * 获取 Http 缓冲图像
     */
    fun getHttpBufferedImage(url: String): InputStream? {
        try {
            val connection: HttpURLConnection = URL(url).openConnection() as HttpURLConnection
            connection.readTimeout = 5000
            connection.connectTimeout = 5000
            connection.requestMethod = "GET"
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                return connection.inputStream
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 调整图片分辨率
     * 地图最佳显示分辨率为128*128
     */
    fun zoomImage(bufferedImage: BufferedImage, width: Int = 128, height: Int = 128): BufferedImage {
        //读取图片
        val wRatio = width * 1.0 / bufferedImage.width
        val hRatio = height * 1.0 / bufferedImage.height
        val ato = AffineTransformOp(AffineTransform.getScaleInstance(wRatio, hRatio), null)
        return ato.filter(bufferedImage, null)
    }

}