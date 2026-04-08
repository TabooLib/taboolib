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
                val (spigotName, mojangName) = matchName(name)
                if (spigotName == null || mojangName == null) {
                    saveField(namespace, field, field)
                    return field
                }
                // 先尝试按照 Mojang Deobf 查找 Mojang Obf
                var find = paperMapping.fields.find { it.path == mojangName && it.translateName == field }?.mojangName
                // 如果找不到，则按照 Spigot Deobf 查找 Mojang Obf
                if (find == null) {
                    find = spigotMapping.fields.find { it.path == spigotName && it.translateName == field }?.mojangName
                }
                // 如果还找不到，可能就是 Mojang Obf 本身了
                val value = find ?: field
                saveField(namespace, field, value)
                value
            }
        }
        return field
    }

    override fun method(name: String, method: String, vararg parameter: Any?): String {
        // 1.18 开始方法混淆
        // 2026/4/1 更改：改为从 1.17 开始判断，因为 1.17 的方法名存在 Mojang Deobf 版本
        if (isUniversal) {
            val namespace = "$name#$method(${parameter.joinToString(",") { it?.javaClass?.name.toString() }})"
            return if (methodRemapCacheMap.containsKey(namespace)) {
                methodRemapCacheMap[namespace]!!
            } else {
                val (spigotName, mojangName) = matchName(name)
                if (spigotName == null || mojangName == null) {
                    saveMethod(namespace, method, method, null)
                    return method
                }
                val pArray: Array<Any?> = arrayOf(*parameter)
                // 先尝试按照 Mojang Deobf 查找 Mojang Obf
                var findObf = paperMapping.methods.find {
                    // 判断方法描述符获取准确
                    it.path == mojangName && it.translateName == method && RemapHelper.checkParameterType(pArray, it.descriptor)
                }
                // 如果找不到，则按照 Spigot Deobf 查找 Mojang Deobf
                if (findObf == null) {
                    findObf = spigotMapping.methods.find {
                        it.path == spigotName && it.translateName == method && RemapHelper.checkParameterType(pArray, it.descriptor)
                    }
                }
                // 如果还找不到，可能就是 Mojang Obf 本身了
                val value = findObf?.mojangName ?: method
                saveMethod(namespace, method, value, findObf?.descriptor)
                value
            }
        }
        return method
    }
}