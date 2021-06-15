package taboolib.common;

import taboolib.common.env.KotlinEnv;
import taboolib.common.platform.PlatformFactory;

/**
 * TabooLib
 * taboolib.common.TabooLibCommon
 *
 * @author sky
 * @since 2021/6/15 2:45 下午
 */
public class TabooLibCommon {

    public static void init() {
        // 初始化 Kotlin
        KotlinEnv.init();
        // 初始化跨平台接口
        PlatformFactory.INSTANCE.init();
    }
}
