package taboolib.common.reflect

/**
 * TabooLib
 * taboolib.common.reflect.ReflexMapping
 *
 * @author sky
 * @since 2021/6/18 1:56 下午
 */
interface ReflexRemapper {

    /**
     * @param name net.minecraft.server.v1_16_R1.EntityPlayer
     * @param field a
     */
    fun field(name: String, field: String): String

    /**
     * @param name net.minecraft.server.v1_16_R1.EntityPlayer
     * @param method a
     */
    fun method(name: String, method: String, vararg parameter: Any?): String
}