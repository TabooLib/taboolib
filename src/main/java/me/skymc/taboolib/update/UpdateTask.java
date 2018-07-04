package me.skymc.taboolib.update;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.Main;
import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.fileutils.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author sky
 * @since 2018年2月23日 下午10:39:14
 */
public class UpdateTask {

    private static boolean haveUpdate = false;
    private static double newVersion = 0;

    public static boolean isHaveUpdate() {
        return haveUpdate;
    }

    public static double getNewVersion() {
        return newVersion;
    }

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

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (Main.getInst().getConfig().getBoolean("UPDATE-DOWNLOAD", false)) {
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "taboolib updatePlugin");
                                }
                            }
                        }.runTask(Main.getInst());
                    }
                }
            }
        }.runTaskTimerAsynchronously(Main.getInst(), 100, 20 * 60 * 60 * 6);
    }
}
