package me.skymc.taboolib.update;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ilummc.tlib.resources.TLocale;
import com.ilummc.tlib.util.Strings;
import me.skymc.taboolib.Main;
import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.fileutils.FileUtils;
import me.skymc.taboolib.player.PlayerUtils;
import me.skymc.taboolib.plugin.PluginUtils;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

/**
 * @author sky
 * @since 2018年2月23日 下午10:39:14
 */
public class UpdateTask {

    private static boolean haveUpdate = false;
    private static double newVersion = 0;

    public UpdateTask() {
        new BukkitRunnable() {

            @Override
            public void run() {
                if (!Main.getInst().getConfig().getBoolean("UPDATE-CHECK")) {
                    return;
                }
                String value = FileUtils.getStringFromURL("https://api.github.com/repos/Bkm016/TabooLib/tags", null);
                if (value == null) {
                    TLocale.Logger.error("UPDATETASK.VERSION-FAIL");
                    return;
                }
                JsonElement json = new JsonParser().parse(value);
                if (json.isJsonArray()) {
                    JsonObject latestObject = json.getAsJsonArray().get(0).getAsJsonObject();
                    newVersion = latestObject.get("name").getAsDouble();
                    if (TabooLib.getPluginVersion() >= newVersion) {
                        TLocale.Logger.info("UPDATETASK.VERSION-LATEST");
                    } else {
                        haveUpdate = true;
                        TLocale.Logger.info("UPDATETASK.VERSION-OUTDATED", String.valueOf(TabooLib.getPluginVersion()), String.valueOf(newVersion));
                        Bukkit.getScheduler().runTask(TabooLib.instance(), () -> updatePlugin(true));
                    }
                }
            }
        }.runTaskTimerAsynchronously(Main.getInst(), 100, 20 * 60 * 60 * 6);
    }

    public static boolean isHaveUpdate() {
        return haveUpdate;
    }

    public static double getNewVersion() {
        return newVersion;
    }

    public static void updatePlugin(boolean shutdown) {
        if (!UpdateTask.isHaveUpdate()) {
            TLocale.Logger.info("COMMANDS.TABOOLIB.UPDATEPLUGIN.UPDATE-NOT-FOUND");
            return;
        }
        if (PlayerUtils.getOnlinePlayers().size() > 0) {
            TLocale.Logger.info("COMMANDS.TABOOLIB.UPDATEPLUGIN.PLAYER-ONLINE");
            return;
        }
        File pluginFile = PluginUtils.getPluginFile(Main.getInst());
        if (pluginFile == null) {
            TLocale.Logger.info("COMMANDS.TABOOLIB.UPDATEPLUGIN.FILE-NOT-FOUND");
            return;
        }
        FileUtils.download("https://github.com/Bkm016/TabooLib/releases/download/" + newVersion + "/TabooLib-" + newVersion + ".jar", pluginFile);
        TLocale.Logger.info("COMMANDS.TABOOLIB.UPDATEPLUGIN.UPDATE-SUCCESS");
        if (shutdown) {
            Bukkit.shutdown();
        }
    }
}
