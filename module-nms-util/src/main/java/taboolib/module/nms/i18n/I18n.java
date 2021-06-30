package taboolib.module.nms.i18n;

import com.google.common.collect.Maps;
import taboolib.common.LifeCycle;
import taboolib.common.platform.Awake;
import taboolib.module.nms.MinecraftVersion;

import java.util.Map;

/**
 * 原版语言文件工具
 *
 * @author sky
 * @since 2020-04-04 19:33
 */
public class I18n {

    public static final Map<Integer, I18nBase> VERSION = Maps.newHashMap();

    private static I18nBase base;

    static {
        VERSION.put(7, I18n11700.INSTANCE); // 1.15
        VERSION.put(8, I18n11700.INSTANCE); // 1.16
        VERSION.put(9, I18n11700.INSTANCE); // 1.17
    }

    @Awake(LifeCycle.INIT)
    static void init() {
        base = VERSION.getOrDefault(MinecraftVersion.INSTANCE.getMajor(), I18nOrigin.INSTANCE);
        base.init();
    }

    public static I18nBase get() {
        return base;
    }
}
