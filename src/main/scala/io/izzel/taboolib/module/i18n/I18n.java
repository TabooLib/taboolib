package io.izzel.taboolib.module.i18n;

import com.google.common.collect.Maps;
import io.izzel.taboolib.Version;
import io.izzel.taboolib.module.i18n.version.I18n11601;
import io.izzel.taboolib.module.i18n.version.I18n20w14a;
import io.izzel.taboolib.module.i18n.version.I18nOrigin;
import io.izzel.taboolib.module.inject.TFunction;

import java.util.Map;

/**
 * @Author sky
 * @Since 2020-04-04 19:33
 */
public class I18n {

    public static final Map<Version, I18nBase> VERSION = Maps.newHashMap();

    private static I18nBase base;

    static {
        VERSION.put(Version.v1_7, I18nOrigin.INSTANCE);
        VERSION.put(Version.v1_8, I18nOrigin.INSTANCE);
        VERSION.put(Version.v1_9, I18nOrigin.INSTANCE);
        VERSION.put(Version.v1_10, I18nOrigin.INSTANCE);
        VERSION.put(Version.v1_11, I18nOrigin.INSTANCE);
        VERSION.put(Version.v1_12, I18nOrigin.INSTANCE);
        VERSION.put(Version.v1_13, I18nOrigin.INSTANCE);
        VERSION.put(Version.v1_14, I18nOrigin.INSTANCE);
        VERSION.put(Version.v1_15, I18n20w14a.INSTANCE);
        VERSION.put(Version.v1_16, I18n11601.INSTANCE);
    }

    @TFunction.Init
    static void init() {
        base = VERSION.getOrDefault(Version.getCurrentVersion(), I18nOrigin.INSTANCE);
        base.init();
    }

    public static I18nBase get() {
        return base;
    }
}
