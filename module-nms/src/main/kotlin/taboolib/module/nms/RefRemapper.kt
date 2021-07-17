package taboolib.module.nms

import taboolib.common.reflect.ReflexRemapper

/**
 * TabooLib
 * taboolib.module.nms.RefRemapper
 *
 * @author sky
 * @since 2021/6/18 5:43 下午
 */
object RefRemapper : ReflexRemapper {

    override fun field(name: String, field: String): String {
        if (MinecraftVersion.isUniversal) {
            return MinecraftVersion.mapping.fields.firstOrNull { it.path == name && it.translateName == field }?.mojangName ?: field
        }
        return field
    }

    override fun method(name: String, method: String): String {
        return method
    }
}