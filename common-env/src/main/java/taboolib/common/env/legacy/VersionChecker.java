package taboolib.common.env.legacy;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * TabooLib
 * taboolib.common.env.VersionChecker
 *
 * @author 坏黑
 * @since 2023/3/31 16:37
 */
public class VersionChecker {

    private static final File checkFile = new File("version.lock");

    /**
     * 是否需要检查更新
     * 距离上次版本检查的时间是否超过 7 天
     */
    public static boolean isOutdated() {
        return System.currentTimeMillis() - getLatestCheckTime() > TimeUnit.DAYS.toMillis(7);
    }

    /** 获取最后一次检查的时间 */
    public static long getLatestCheckTime() {
        return checkFile.lastModified();
    }

    /** 更新最后一次检查的时间 */
    public static void updateCheckTime() {
        if (checkFile.exists()) {
            checkFile.setLastModified(System.currentTimeMillis());
        } else {
            try {
                checkFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
