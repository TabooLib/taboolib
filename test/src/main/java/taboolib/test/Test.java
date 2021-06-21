package taboolib.test;

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
        RuntimeEnv.setup("yaml")
                .add("org.yaml", "snakeyaml", "1.28", "7cae037c3014350c923776548e71c9feb7a69259", "sha-1")
                .run();
        ArrayList<Object> strings = new ArrayList<>();
        strings.add("a");
        strings.add("b");
        Map<Object, Object> map = new HashMap<>();
        map.put("c", "d");
        map.put("e", null);
        strings.add(map);

        System.out.println(SecuredFile.Companion.dumpAll("key", strings));
        System.out.println("---");
        System.out.println(SecuredFile.Companion.dumpAll("key", map));
        System.out.println("---");
        System.out.println(SecuredFile.Companion.dumpAll("key", new HashMap()));
        System.out.println("---");
        System.out.println(SecuredFile.Companion.dumpAll("key", "aabbcc"));
    }
}
