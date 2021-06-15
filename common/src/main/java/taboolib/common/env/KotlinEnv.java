package taboolib.common.env;

import taboolib.common.env.runtime.RuntimeEnv;

/**
 * TabooLib
 * taboolib.common.KotlinEnv
 *
 * @author sky
 * @since 2021/6/15 2:28 下午
 */
public class KotlinEnv {

    public static final String GROUP = "org.jetbrains.kotlin";
    public static final String VERSION = "1.5.20-RC";

    /**
     * 初始化 Kotlin 运行环境
     * 通过 Sonatype Repository 源下载依赖文件并进行 sha-1 判断
     */
    public static void init() {
        RuntimeEnv.setup("Kotlin (" + VERSION + ")")
                .check("kotlin.KotlinVersion")
                .add(GROUP, "kotlin-stdlib", VERSION, "7fd0e9a9b711fe1766b6665d4cb5ae54c428df39", "SHA-1")
                .add(GROUP, "kotlin-stdlib-jdk7", VERSION, "6ca2c5b7ff492294d3834020ca2224303a28a75b", "SHA-1")
                .add(GROUP, "kotlin-stdlib-jdk8", VERSION, "9add2c5bf197ca50298518b9bf047f40b34c189e", "SHA-1")
                .add(GROUP, "kotlin-reflect", VERSION, "4ec0e80ad7fa2ca4f4e90b21864e8df440b8a8bc", "SHA-1")
                .run();
    }

    /**
     * 当前是否运行在 Kotlin 环境下
     */
    public static boolean isKotlinEnvironment() {
        return ClassAppender.INSTANCE.isExists("kotlin.KotlinVersion");
    }
}
