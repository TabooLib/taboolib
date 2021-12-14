package taboolib.module.nms

import taboolib.common.env.RuntimeResource
import taboolib.common.env.RuntimeResources

/**
 * TabooLib
 * taboolib.module.nms.MappingFile
 *
 * @author sky
 * @since 2021/7/17 9:04 下午
 */
@RuntimeResources(
    RuntimeResource(
        value = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/bukkit-e3c5450d-combined.csrg",
        hash = "ec52bfc2822dd8385c619f6e80e106baab1c1454",
        zip = true,
        tag = "1.17:combined"
    ),
    RuntimeResource(
        value = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/bukkit-e3c5450d-fields.csrg",
        hash = "44caa1f63bd20d807bd92d13d2fe291b482c0771",
        zip = true,
        tag = "1.17:fields"
    ),
    RuntimeResource(
        value = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/bukkit-00fabbe5-combined.csrg",
        hash = "a1a36e589321cd782aa9f0917bc0a1516a69de3d",
        zip = true,
        tag = "1.17.1:combined"
    ),
    RuntimeResource(
        value = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/bukkit-00fabbe5-fields.csrg",
        hash = "6e515ad1b4cd49e93e26380e4deca8b876a517a7",
        zip = true,
        tag = "1.17.1:fields"
    ),
    RuntimeResource(
        value = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/bukkit-1.18-cl.csrg",
        hash = "9a3742d6b84542d263c7309fb5a23066a113e307",
        zip = true,
        tag = "1.18:combined"
    ),
    RuntimeResource(
        value = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/bukkit-8e9479b6-members.csrg",
        hash = "805efea073022d30cab12cd511513751af80789c",
        zip = true,
        tag = "1.18:fields"
    ),
    RuntimeResource(
        value = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/bukkit-1.18.1-cl.csrg",
        hash = "9a3742d6b84542d263c7309fb5a23066a113e307",
        zip = true,
        tag = "1.18.1:combined"
    ),
    RuntimeResource(
        value = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/bukkit-e0c6d16a-members.csrg",
        hash = "6d0d8df7538d9e0006ff2f9c01a4125d699e857b",
        zip = true,
        tag = "1.18.1:fields"
    )
)
class MappingFile(val combined: String, val fields: String) {

    companion object {

        val files = MappingFile::class.java.getDeclaredAnnotation(RuntimeResources::class.java).value
            .groupBy { it.tag.split(':')[0] }
            .map {
                it.key to MappingFile(
                    it.value.first { a -> a.tag.split(':')[1] == "combined" }.hash,
                    it.value.first { a -> a.tag.split(':')[1] == "fields" }.hash
                )
            }.toMap()
    }
}