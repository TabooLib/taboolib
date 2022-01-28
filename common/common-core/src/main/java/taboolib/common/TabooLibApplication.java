package taboolib.common;

import taboolib.common.boot.Booster;

/**
 * TabooLib
 * taboolib.common.TabooLibApplication
 *
 * @author 坏黑
 * @since 2022/1/25 1:56 AM
 */
@Isolated
public class TabooLibApplication {

    static final Booster booster = TabooLib.booster();

    TabooLibApplication() {
    }

    /**
     * 用于测试的快速启动方法
     * 会按顺序触发 CONST、INIT、LOAD、ENABLE、ACTIVE 生命周期
     */
    public static void startsNow() {
        booster.proceed(LifeCycle.CONST);
        booster.proceed(LifeCycle.INIT);
        booster.proceed(LifeCycle.LOAD);
        booster.proceed(LifeCycle.ENABLE);
        booster.proceed(LifeCycle.ACTIVE);
    }

    /**
     * 用于测试的快速注销方法
     * 会触发 DISABLE 生命周期
     */
    public static void disableNow() {
        booster.proceed(LifeCycle.DISABLE);
    }
}
