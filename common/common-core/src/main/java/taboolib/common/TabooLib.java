package taboolib.common;

import org.jetbrains.annotations.NotNull;
import taboolib.common.boot.Booster;
import taboolib.common.boot.Monitor;
import taboolib.common.boot.SimpleServiceLoader;
import taboolib.common.platform.Platform;

/**
 * TabooLib
 * taboolib.common.TabooLib
 *
 * @author sky
 * @since 2021/6/15 2:45 下午
 */
public class TabooLib {

    static final Booster booster = SimpleServiceLoader.load(Booster.class);

    TabooLib() {
    }

    @NotNull
    public static Booster booster() {
        return booster;
    }

    @NotNull
    public static Monitor monitor() {
        return booster().getMonitor();
    }

    @NotNull
    public static Platform runningPlatform() {
        return booster().getPlatform();
    }
}
