package taboolib.common;

import org.jetbrains.annotations.NotNull;
import taboolib.common.boot.Booster;
import taboolib.common.boot.Monitor;
import taboolib.common.boot.SimpleServiceLoader;
import taboolib.common.env.RuntimeDependency;
import taboolib.common.platform.Platform;
import taboolib.internal.SimpleBooster;

/**
 * TabooLib
 * taboolib.common.TabooLib
 *
 * @author sky
 * @since 2021/6/15 2:45 下午
 */
@RuntimeDependency(value = "!com.google.code.gson:gson:2.8.7", test = "!com.google.gson.JsonElement")
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
