package taboolib.module.nms

fun ItemTagData.clone(): ItemTagData {
    return when (type) {
        ItemTagType.END -> ItemTagData(type, null)
        ItemTagType.BYTE,
        ItemTagType.SHORT,
        ItemTagType.INT,
        ItemTagType.LONG,
        ItemTagType.FLOAT,
        ItemTagType.DOUBLE,
        ItemTagType.STRING -> ItemTagData(type, unsafeData())
        ItemTagType.BYTE_ARRAY -> ItemTagData(type, asByteArray().copyOf())
        ItemTagType.INT_ARRAY -> ItemTagData(type, asIntArray().copyOf())
        ItemTagType.LIST -> {
            val list = ItemTagList()
            asList().forEach { list.add(it.clone()) }
            list
        }
        ItemTagType.COMPOUND -> {
            val compound = ItemTag()
            asCompound().forEach { (k, v) -> compound[k] = v.clone() }
            compound
        }
        else -> error("unsupported tag type")
    }
}