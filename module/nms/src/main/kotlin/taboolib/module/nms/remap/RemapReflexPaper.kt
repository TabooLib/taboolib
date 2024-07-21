package taboolib.module.nms.remap

import taboolib.module.nms.MinecraftVersion

/**
 * TabooLib
 * taboolib.module.nms.remap.RefRemapper
 *
 * 只有 Paper 1.20.6+ 才会启用该类
 *
 * @author sky
 * @since 2021/6/18 5:43 下午
 */
@Suppress("DuplicatedCode")
class RemapReflexPaper : RemapReflex() {

    override fun field(name: String, field: String): String {
        val namespace = "$name#$field"
        return if (fieldRemapCacheMap.containsKey(namespace)) {
            fieldRemapCacheMap[namespace]!!
        } else {
            val (spigotName, mojangName) = matchName(name)
            if (spigotName == null || mojangName == null) {
                saveField(name, field, field)
                return field
            }
            // 还原
            val obf = spigotMapping.fields.find { it.path == spigotName && (it.translateName == field || it.mojangName == field) }?.mojangName
            // 重映射
            val deobf = paperMapping.fields.find { it.path == mojangName && it.mojangName == obf }?.translateName ?: field
            saveField(name, field, deobf)
            deobf
        }
    }

    override fun method(name: String, method: String, vararg parameter: Any?): String {
        val namespace = "$name#$method(${parameter.joinToString(",") { it?.javaClass?.name.toString() }})"
        return if (methodRemapCacheMap.containsKey(namespace)) {
            methodRemapCacheMap[namespace]!!
        } else {
            val (spigotName, mojangName) = matchName(name)
            if (spigotName == null || mojangName == null) {
                saveMethod(name, method, method, null)
                return method
            }
            val pArray: Array<Any?> = arrayOf(*parameter)
            // 还原
            val findObf = spigotMapping.methods.find {
                // 判断方法描述符获取准确方法
                it.path == spigotName && (it.translateName == method || it.mojangName == method) && RemapHelper.checkParameterType(pArray, it.descriptor)
            }
            val obf = findObf?.mojangName ?: method
            // 重映射
            val findDeobf = paperMapping.methods.find {
                it.path == mojangName && it.mojangName == obf && RemapHelper.checkParameterType(pArray, it.descriptor)
            }
            val deobf = findDeobf?.translateName ?: method
            saveMethod(name, method, deobf, "${findObf?.descriptor}->${findDeobf?.descriptor} (${parameter.joinToString(",") { p -> p?.javaClass?.name.toString() }})")
            deobf
        }
    }

    /**
     * 这里存在一个潜在问题，与 NMSProxy 不同的是无法确认它来自何种对照表
     * 因此要从两边猜
     */
    fun matchName(name: String): Pair<String?, String?> {
        val className = name.replace('/', '.')
        var spigotName = paperMapping.classMapMojangToSpigot[className]
        var mojangName: String? = null
        // 不为空说明 name 是 Mojang 名
        if (spigotName != null) {
            mojangName = className
        } else {
            spigotName = className
            mojangName = paperMapping.classMapSpigotToMojang[className]
        }
        return spigotName to mojangName
    }

    fun translate(key: String): String {
        return MinecraftVersion.paperMapping.classMapSpigotToMojang[key.replace('/', '.')] ?: key
    }
}