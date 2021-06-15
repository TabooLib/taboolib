package taboolib.module.dependency.env;

import taboolib.common.env.RuntimeEnv;

/**
 * TabooLib
 * taboolib.common5.env.AsmEnv
 *
 * @author sky
 * @since 2021/6/15 2:28 下午
 */
public class AsmEnv {

    public static final String GROUP = "org.ow2.asm";
    public static final String VERSION = "9.1";

    /**
     * 初始化 Kotlin 运行环境
     * 通过 Sonatype Repository 源下载依赖文件并进行 sha-1 判断
     */
    public static void init() {
        RuntimeEnv.setup("asm (" + VERSION + ")")
                .check("org.objectweb.asm.ClassVisitor")
                .add(GROUP, "asm", VERSION, "a99500cf6eea30535eeac6be73899d048f8d12a8", "SHA-1")
                .add(GROUP, "asm-commons", VERSION, "8b971b182eb5cf100b9e8d4119152d83e00e0fdd", "SHA-1")
                .run();
    }
}
