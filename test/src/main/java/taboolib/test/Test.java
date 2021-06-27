package taboolib.test;

import kotlin.jvm.internal.Intrinsics;
import taboolib.common.TabooLibCommon;
import taboolib.common.env.RuntimeEnv;
import taboolib.module.configuration.SecuredFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * TabooLib
 * taboolib.common5.Test
 *
 * @author sky
 * @since 2021/6/15 4:37 下午
 */
public class Test {

    public static void main(String[] args) {
        TabooLibCommon.init();
        Intrinsics.checkNotNull(null, "123");
        System.out.println("112233");
    }
}
