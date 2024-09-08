package taboolib.module.nms.remap

/**
 * TabooLib
 * taboolib.module.nms.remap.RefRemapper
 *
 * @author sky
 * @since 2021/6/18 5:43 下午
 */
@Suppress("DuplicatedCode")
class RemapReflexSpigot : RemapReflex() {

    override fun field(name: String, field: String): String {
        // 1.17 开始字段混淆
        if (isUniversal) {
            val namespace = "$name#$field"
            return if (fieldRemapCacheMap.containsKey(namespace)) {
                fieldRemapCacheMap[namespace]!!
            } else {
                // 还原
                val value = spigotMapping.fields.find { it.path == name && it.translateName == field }?.mojangName ?: field
                saveField(name, field, value)
                value
            }
        }
        return field
    }

    override fun method(name: String, method: String, vararg parameter: Any?): String {
        // 1.18 开始方法混淆
        if (major >= 10) {
            val namespace = "$name#$method(${parameter.joinToString(",") { it?.javaClass?.name.toString() }})"
            return if (methodRemapCacheMap.containsKey(namespace)) {
                methodRemapCacheMap[namespace]!!
            } else {
                val pArray: Array<Any?> = arrayOf(*parameter)
                // 还原
                val find = spigotMapping.methods.find {
                    // 判断方法描述符获取准确方法
                    it.path == name && it.translateName == method && RemapHelper.checkParameterType(pArray, it.descriptor)
                }
                val value = find?.mojangName ?: method
                saveMethod(name, method, value, find?.descriptor)
                value
            }
        }
        return method
    }
}