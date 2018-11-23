package me.skymc.taboolib.common.nms;

import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.commands.builder.SimpleCommandBuilder;
import me.skymc.taboolib.common.function.TFunction;
import me.skymc.taboolib.common.versioncontrol.SimpleVersionControl;
import org.bukkit.entity.Player;

/**
 * @Author 坏黑
 * @Since 2018-11-09 14:38
 */
@TFunction(enable = "init")
public abstract class NMSHandler {

    private static NMSHandler handler;

    static void init() {
        try {
            handler = (NMSHandler) SimpleVersionControl.createNMS("me.skymc.taboolib.common.nms.NMSHandlerImpl").translate().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        SimpleCommandBuilder.create("title", TabooLib.instance())
                .execute((sender, args) -> {
                    handler.sendTitle((Player) sender, "TabooLib", 10, 40, 10, "author Bkm016", 10, 40, 10);
                    return true;
                }).build();
    }

    abstract public void sendTitle(Player player, String title, int titleFadein, int titleStay, int titleFadeout, String subtitle, int subtitleFadein, int subtitleStay, int subtitleFadeout);

    abstract public void sendActionBar(Player player, String text);

    public static NMSHandler getHandler() {
        return handler;
    }
}
