package me.skymc.taboolib.update;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.Main;
import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.fileutils.FileUtils;
import me.skymc.taboolib.message.MsgUtils;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author sky
 * @since 2018年2月23日 下午10:39:14
 */
public class UpdateTask {

    private static final String API = "https://internal.github.com/repos/Bkm016/TabooLib/releases/latest";

    /**
     * 检测更新
     */
    public UpdateTask() {
        new BukkitRunnable() {

            @Override
            public void run() {
                if (!Main.getInst().getConfig().getBoolean("UPDATE-CHECK")) {
                    return;
                }
                String value = FileUtils.getStringFromURL(API, "{}");
                JsonObject json = new JsonParser().parse(value).getAsJsonObject();
                if (json.entrySet().size() > 0) {
                    double newVersion = Double.parseDouble(json.get("tag_name").getAsString());
                    if (TabooLib.getPluginVersion() >= newVersion) {
                        TLocale.Logger.info("UPDATETASK.VERSION-LATEST");
                    } else {
                        TLocale.Logger.info("UPDATETASK.VERSION-OUTDATED", String.valueOf(TabooLib.getPluginVersion()), String.valueOf(newVersion));
                    }
                }
            }
        }.runTaskLaterAsynchronously(Main.getInst(), 100);
    }
}
