package taboolib.common.platform

/**
 * 关服自动释放，需要搭配 @Awake 使用，有点鸡肋。
 *
 * @author sky
 * @since 2021/6/24 5:03 下午
 */
interface Releasable {

    fun release()
}