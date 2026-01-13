package taboolib.module.nms

/**
 * TabooLib
 * taboolib.module.nms.ItemComponent
 *
 * @author 晓劫
 * @since 2025/11/20 11:24
 */
interface ItemComponent {

    fun getTagData(nbtTag: Any): ItemTagData?

    fun itemTagToBukkitCopy(nbtTag: Any): ItemTagData {
        return NMSItemTag.instance.itemTagToBukkitCopy(nbtTag)
    }

    companion object {
        val instance by lazy {
            nmsProxy<ItemComponent>()
        }
    }
}