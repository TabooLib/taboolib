package taboolib.module.nms

/**
 * Adyeshach
 * taboolib.module.nms.DataSerializerFactory
 *
 * @author 坏黑
 * @since 2022/12/12 23:04
 */
interface DataSerializerFactory {

    fun newSerializer(): DataSerializer
}

/**
 * 创建一个 [DataSerializer]
 */
fun dataSerializerBuilder(builder: DataSerializer.() -> Unit = {}): DataSerializer {
    return nmsProxy<DataSerializerFactory>().newSerializer().also(builder)
}

typealias NMS21DataWatcher = net.minecraft.network.syncher.DataWatcher
typealias NMS21DataWatcherItem<T> = net.minecraft.network.syncher.DataWatcher.Item<T>
typealias NMS21PacketDataSerializer = net.minecraft.network.PacketDataSerializer

typealias NMS16DataWatcher = net.minecraft.server.v1_16_R3.DataWatcher
typealias NMS16DataWatcherItem<T> = net.minecraft.server.v1_16_R3.DataWatcher.Item<T>
typealias NMS16PacketDataSerializer = net.minecraft.server.v1_16_R3.PacketDataSerializer