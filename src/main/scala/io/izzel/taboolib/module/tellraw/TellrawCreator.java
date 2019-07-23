package io.izzel.taboolib.module.tellraw;

import io.izzel.taboolib.module.inject.TFunction;
import io.izzel.taboolib.module.inject.TSchedule;
import io.izzel.taboolib.module.lite.SimpleVersionControl;
import io.izzel.taboolib.module.tellraw.internal.AbstractTellraw;
import org.bukkit.Bukkit;

/**
 * @Author 坏黑
 * @Since 2018-11-07 22:58
 */
@TFunction(enable = "init")
public class TellrawCreator {

    private static AbstractTellraw abstractTellraw;
    private static boolean viaVersionLoaded;
    private static boolean protocolSupportLoaded;

    public static void init() {
        try {
            abstractTellraw = (AbstractTellraw) SimpleVersionControl.createNMS("io.izzel.taboolib.module.tellraw.internal.InternalTellraw").translate().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @TSchedule
    static void tick() {
        viaVersionLoaded = Bukkit.getPluginManager().getPlugin("ViaVersion") != null;
        protocolSupportLoaded = Bukkit.getPluginManager().getPlugin("ProtocolSupport") != null;
    }

    // *********************************
    //
    //        Getter and Setter
    //
    // *********************************

    public static AbstractTellraw getAbstractTellraw() {
        return abstractTellraw;
    }

    public static boolean isViaVersionLoaded() {
        return viaVersionLoaded;
    }

    public static boolean isProtocolSupportLoaded() {
        return protocolSupportLoaded;
    }
}
