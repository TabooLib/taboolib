package me.skymc.taboolib.json.tellraw;

import me.skymc.taboolib.common.loader.Instantiable;
import me.skymc.taboolib.common.versioncontrol.SimpleVersionControl;
import me.skymc.taboolib.json.tellraw.internal.AbstractTellraw;
import org.bukkit.Bukkit;

/**
 * @Author 坏黑
 * @Since 2018-11-07 22:58
 */
@Instantiable("TabooLib|TellrawCreator")
public class TellrawCreator {

    private static AbstractTellraw abstractTellraw;
    private static boolean viaVersionLoaded;
    private static boolean protocolSupportLoaded;

    public TellrawCreator() {
        viaVersionLoaded = Bukkit.getPluginManager().getPlugin("ViaVersion") != null;
        protocolSupportLoaded = Bukkit.getPluginManager().getPlugin("ProtocolSupport") != null;
        try {
            abstractTellraw = (AbstractTellraw) SimpleVersionControl.createNMS("me.skymc.taboolib.json.tellraw.internal.InternalTellraw").translate().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
