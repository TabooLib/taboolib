package taboolib.common5

import taboolib.common.platform.PlatformInstance
import taboolib.common5.env.AsmEnv

/**
 * TabooLib
 * taboolib.common5.taboolib.common5.Common5
 *
 * @author sky
 * @since 2021/6/15 4:27 下午
 */
@PlatformInstance
object Common5 {

    init {
        AsmEnv.init()
    }
}