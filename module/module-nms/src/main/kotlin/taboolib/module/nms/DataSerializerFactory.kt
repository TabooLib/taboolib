package taboolib.module.nms

import taboolib.common.util.unsafeLazy

/**
 * Adyeshach
 * taboolib.module.nms.DataSerializerFactory
 *
 * @author 坏黑
 * @since 2022/12/12 23:04
 */
interface DataSerializerFactory {

    fun newSerializer(): DataSerializer

    companion object {

        @JvmStatic
        val instance by unsafeLazy { nmsProxy<DataSerializerFactory>() }
    }
}