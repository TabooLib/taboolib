package taboolib.module.map

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

object BuildMap {

    fun createMap(url: String, name: String?, lore: List<String>?, width: Int = 128, height: Int = 128): CreateMap {
        val image =
            ImageUtils.zoomImage(ImageIO.read(ImageUtils.getHttpBufferedImage(url)), width, height)
        return CreateMap(image, name, lore)
    }

    fun createMap(
        image: BufferedImage,
        name: String,
        lore: List<String>,
    ): CreateMap {
        return CreateMap(ImageUtils.zoomImage(image), name, lore)
    }

    fun createMap(
        image: File,
        name: String,
        lore: List<String>,
    ): CreateMap {
        return CreateMap(ImageUtils.zoomImage(ImageIO.read(image)), name, lore)
    }

    fun createMap(url: String, width: Int = 128, height: Int = 128): CreateMap {
        return createMap(url, null, null, width, height)
    }

}