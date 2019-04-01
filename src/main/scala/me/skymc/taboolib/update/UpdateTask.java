package me.skymc.taboolib.update;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.Main;
import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.common.schedule.TSchedule;
import me.skymc.taboolib.fileutils.FileUtils;
import me.skymc.taboolib.player.PlayerUtils;
import me.skymc.taboolib.plugin.PluginUtils;
import org.bukkit.Bukkit;

import java.io.*;

/**
 * @author sky
 * @since 2018年2月23日 下午10:39:14
 */
public class UpdateTask {

    private static double newVersion = 0;
    private static double length = -1;
    private static int updateLocationUsing;
    private static String[][] updateLocation = {
            {
                    "https://api.github.com/repos/Bkm016/TabooLib/releases",
                    "https://github.com/Bkm016/TabooLib/releases/download/?/TabooLib-?.jar"
            },
            {
                    "https://gitee.com/bkm016/TabooLibCloud/raw/master/release.json",
                    "https://gitee.com/bkm016/TabooLibCloud/raw/master/core/TabooLib.jar"
            }
    };

    @TSchedule(async = true, delay = 100, period = 20 * 60 * 60 * 6)
    static void update() {
        if (!Main.getInst().getConfig().getBoolean("UPDATE-CHECK", true)) {
            return;
        }
        for (int i = 0; i < updateLocation.length; i++) {
            String[] location = updateLocation[i];
            String value = FileUtils.getStringFromURL(location[0], null);
            if (value == null) {
                continue;
            }
            JsonElement json = new JsonParser().parse(value);
            if (json.isJsonArray()) {
                JsonObject releaseData = json.getAsJsonArray().get(0).getAsJsonObject();
                updateLocationUsing = i;
                newVersion = releaseData.get("tag_name").getAsDouble();
                // 获取文件长度
                for (JsonElement assetData : releaseData.getAsJsonArray("assets")) {
                    if (assetData instanceof JsonObject && ((JsonObject) assetData).get("name").getAsString().equals("TabooLib-" + newVersion + ".jar")) {
                        length = ((JsonObject) assetData).get("size").getAsInt();
                    }
                }
                if (TabooLib.getPluginVersion() >= newVersion) {
                    TLocale.Logger.info("UPDATETASK.VERSION-LATEST");
                } else {
                    TLocale.Logger.info("UPDATETASK.VERSION-OUTDATED", String.valueOf(TabooLib.getPluginVersion()), String.valueOf(newVersion));
                    // 是否启用启动下载
                    if (Main.getInst().getConfig().getBoolean("UPDATE-DOWNLOAD", false)) {
                        Bukkit.getScheduler().runTask(TabooLib.instance(), () -> updatePlugin(true, false));
                    }
                }
                return;
            }
        }
        TLocale.Logger.error("UPDATETASK.VERSION-FAIL");
    }

    public static boolean isHaveUpdate() {
        return newVersion > TabooLib.getPluginVersion();
    }

    public static double getNewVersion() {
        return newVersion;
    }

    public static int getUpdateLocationUsing() {
        return updateLocationUsing;
    }

    public static String[][] getUpdateLocation() {
        return updateLocation;
    }

    public static void updatePlugin(boolean shutdown, boolean force) {
        File pluginFile = PluginUtils.getPluginFile(Main.getInst());
        if (pluginFile == null) {
            TLocale.Logger.info("COMMANDS.TABOOLIB.UPDATEPLUGIN.FILE-NOT-FOUND");
            return;
        }
        if (!UpdateTask.isHaveUpdate() && (newVersion == 0 || !force)) {
            TLocale.Logger.info("COMMANDS.TABOOLIB.UPDATEPLUGIN.UPDATE-NOT-FOUND");
            return;
        }
        if (length < 0) {
            TLocale.Logger.error("COMMANDS.TABOOLIB.UPDATEPLUGIN.UPDATE-NOT-FOUND-SIZE");
            return;
        }
        if (PlayerUtils.getOnlinePlayers().size() > 0) {
            TLocale.Logger.info("COMMANDS.TABOOLIB.UPDATEPLUGIN.PLAYER-ONLINE");
            return;
        }
        // 创建临时文件
        File tempFile = new File(Main.getInst().getDataFolder(), "update" + File.separator + "TabooLib-" + newVersion + ".jar");
        FileUtils.createNewFileAndPath(tempFile);
        Bukkit.getScheduler().runTaskAsynchronously(TabooLib.instance(), () -> {
            FileUtils.download(updateLocation[updateLocationUsing][1].replace("?", String.valueOf(newVersion)), tempFile);
            // 判断文件长度是否与标准长度相同
            if (tempFile.length() != length) {
                TLocale.Logger.error("COMMANDS.TABOOLIB.UPDATEPLUGIN.UPDATE-FAILED");
            } else {
                // 覆盖插件文件
                byte[] buf = new byte[1024];
                int len;
                try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(tempFile)); BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(pluginFile))) {
                    while ((len = inputStream.read(buf)) > 0) {
                        outputStream.write(buf, 0, len);
                    }
                    outputStream.flush();
                } catch (Throwable t) {
                    t.printStackTrace();
                    return;
                }
                TLocale.Logger.info("COMMANDS.TABOOLIB.UPDATEPLUGIN.UPDATE-SUCCESS");
                if (shutdown) {
                    Bukkit.shutdown();
                }
            }
        });
    }
}
