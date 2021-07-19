package taboolib.test;

import kotlin.jvm.internal.Intrinsics;
import me.lucko.jarrelocator.JarRelocator;
import me.lucko.jarrelocator.Relocation;
import taboolib.common.TabooLibCommon;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

/**
 * TabooLib
 * taboolib.common5.Test
 *
 * @author sky
 * @since 2021/6/15 4:37 下午
 */
public class Test {

    public static void main(String[] args) throws IOException {
        TabooLibCommon.ENV.setup();
//        File file = new File("/Users/sky/Desktop/kotlin-stdlib-1.5.10.jar");
//        File fileRel = new File("/Users/sky/Desktop/kotlin-stdlib-1.5.10-rel.jar");
//
//        new JarRelocator(file, fileRel, Collections.singletonList(new Relocation("kotlin", "taboolib.library.kotlin1_5_10"))).run();
    }
}
