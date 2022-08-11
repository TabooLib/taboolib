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
    ),
    RuntimeResource(
        value = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/bukkit-1.18.2-cl.csrg",
        hash = "bcf6240fb6a77d326538f61a822334f9ff65c9ec",
        zip = true,
        tag = "1.18.2:combined"
    ),
    RuntimeResource(
        value = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/bukkit-732abad1-members.csrg",
        hash = "e51e094f2888a44d12d0f3d42305afc2675c6748",
        zip = true,
        tag = "1.18.2:fields"
    ),
    RuntimeResource(
        value = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/bukkit-1.19-cl.csrg",
        hash = "44eaa87a517f3fb7661afe387edd68669b782435",
        zip = true,
        tag = "1.19:combined"
    ),
    RuntimeResource(
        value = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/bukkit-2f7944eb-members.csrg",
        hash = "213f64b57f20f414309125b1f4eb7cbbcf159508",
        zip = true,
        tag = "1.19:fields"
    ),
    RuntimeResource(
        value = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/bukkit-1.19.1-cl.csrg",
        hash = "3cee4d607a86f0a7e1dd2a6fb669a2644e4d400c",
        zip = true,
        tag = "1.19.1:combined"
    ),
    RuntimeResource(
        value = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/bukkit-550f788f-members.csrg",
        hash = "709b9250af770537cc8b23f734ac31dbeee6dc6e",
        zip = true,
        tag = "1.19.1:fields"
    ),
    RuntimeResource(
        value = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/bukkit-1.19.2-cl.csrg",
        hash = "c77adbc7fdc2df0b274e2eafecbf3f820ebd710e",
        zip = true,
        tag = "1.19.2:combined"
    ),
    RuntimeResource(
        value = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/bukkit-8b4db36a-members.csrg",
        hash = "069e5d3e081c303231ea453ed1e56ac149917c9e",
        zip = true,
        tag = "1.19.2:fields"
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