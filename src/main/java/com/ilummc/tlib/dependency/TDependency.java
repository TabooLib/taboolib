package com.ilummc.tlib.dependency;

import com.ilummc.eagletdl.EagletTask;
import com.ilummc.eagletdl.ProgressEvent;
import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.Main;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

public class TDependency {

    public static final String MAVEN_REPO = "http://repo.maven.apache.org/maven2";

    /**
     * 请求一个插件作为依赖，这个插件将会在所有已经添加的 Jenkins 仓库、Maven 仓库寻找
     * <p>
     * 阻塞线程进行下载/加载
     *
     * @param args 插件名称，下载地址（可选）
     * @return 是否成功加载了依赖
     */
    public static boolean requestPlugin(String... args) {
        return false;
    }

    /**
     * 请求一个库作为依赖，这个库将会在 Maven Central、oss.sonatype 以及自定义的 Maven 仓库寻找
     * <p>
     * 阻塞线程进行下载/加载
     *
     * @param type 依赖名，格式为 groupId:artifactId:version
     * @return 是否成功加载库，如果加载成功，插件将可以任意调用使用的类
     */
    public static boolean requestLib(String type, String repo, String url) {
        if (type.matches(".*:.*:.*")) {
            String[] arr = type.split(":");
            File file = new File(Main.getInst().getDataFolder(), "/libs/" + String.join("-", arr) + ".jar");
            if (file.exists()) {
                TDependencyLoader.addToPath(Main.getInst(), file);
                return true;
            } else {
                if (downloadMaven(repo, arr[0], arr[1], arr[2], file, url)) {
                    TDependencyLoader.addToPath(Main.getInst(), file);
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    private static boolean downloadMaven(String url, String groupId, String artifactId, String version, File target, String dl) {
        if (Main.getInst().getConfig().getBoolean("OFFLINE-MODE")) {
            TLocale.Logger.warn("DEPENDENCY.OFFLINE-DEPENDENCY-WARN");
            return false;
        }
        AtomicBoolean failed = new AtomicBoolean(false);
        String link = dl.length() == 0 ? url + "/" + groupId.replace('.', '/') + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + ".jar" : dl;
        new EagletTask()
                .url(link)
                .file(target)
                .setThreads(getDownloadPoolSize())
                .setOnError(event -> {
                })
                .setOnConnected(event -> TLocale.Logger.info("DEPENDENCY.DOWNLOAD-CONNECTED",
                        String.join(":", new String[]{groupId, artifactId, version}), ProgressEvent.format(event.getContentLength())))
                .setOnProgress(event -> TLocale.Logger.info("DEPENDENCY.DOWNLOAD-PROGRESS",
                        event.getSpeedFormatted(), event.getPercentageFormatted()))
                .setOnComplete(event -> {
                    if (event.isSuccess()) {
                        TLocale.Logger.info("DEPENDENCY.DOWNLOAD-SUCCESS",
                                String.join(":", new String[]{groupId, artifactId, version}));
                    } else {
                        failed.set(true);
                        TLocale.Logger.error("DEPENDENCY.DOWNLOAD-FAILED",
                                String.join(":", new String[]{groupId, artifactId, version}), link, target.getName());
                    }
                }).start().waitUntil();
        return !failed.get();
    }

    private static int getDownloadPoolSize() {
        return Main.getInst().getConfig().getInt("DOWNLOAD-POOL-SIZE", 4);
    }
}
