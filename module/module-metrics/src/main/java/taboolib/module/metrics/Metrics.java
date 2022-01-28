package taboolib.module.metrics;

import kotlin.Unit;
import taboolib.common.TabooLib;
import taboolib.common.io.FileKt;
import taboolib.common.platform.Platform;
import taboolib.common.platform.function.AdapterKt;
import taboolib.common.platform.function.ExecutorKt;
import taboolib.common.platform.function.IOKt;
import taboolib.module.configuration.SecuredFile;

import java.io.File;
import java.util.Map;
import java.util.UUID;

public class Metrics {

    private MetricsBase metricsBase;

    /**
     * Creates a new Metrics instance.
     *
     * @param serviceId The id of the service. It can be found at <a
     *                  href="https://bstats.org/what-is-my-plugin-id">What is my plugin id?</a>
     */
    public Metrics(int serviceId, String pluginVersion, Platform runningPlatform) {
        if (TabooLib.getRunningPlatform() != runningPlatform) {
            return;
        }
        // Get the config file
        File bStatsFolder = new File(IOKt.getDataFolder().getParentFile(), "bStats");
        File configFile = FileKt.newFile(bStatsFolder, "config.yml", true, false);
        SecuredFile config = SecuredFile.Companion.loadConfiguration(configFile);
        if (!config.contains("serverUUID")) {
            config.set("enabled", true);
            config.set("serverUUID", UUID.randomUUID().toString());
            config.set("logFailedRequests", false);
            config.set("logSentData", false);
            config.set("logResponseStatusText", false);
            config.saveToFile(configFile);
        }
        // Load the data
        boolean enabled = config.getBoolean("enabled", true);
        String serverUUID = config.getString("serverUUID");
        boolean logErrors = config.getBoolean("logFailedRequests", false);
        boolean logSentData = config.getBoolean("logSentData", false);
        boolean logResponseStatusText = config.getBoolean("logResponseStatusText", false);
        String platform = null;
        switch (runningPlatform) {
            case UNKNOWN:
            case BUKKIT:
            case NUKKIT:
                platform = "bukkit";
                break;
            case BUNGEE:
                platform = "bungeecord";
                break;
            case VELOCITY:
                platform = "velocity";
                break;
            case SPONGE_API_7:
            case SPONGE_API_8:
                platform = "sponge";
                break;
            default:
                throw new IllegalStateException("Unsupported");
        }
        metricsBase = new MetricsBase(
                platform,
                serverUUID,
                serviceId,
                enabled,
                json -> appendPlatformData(json, runningPlatform),
                json -> appendServiceData(json, pluginVersion),
                task -> ExecutorKt.submit(false, false, 0, 0, "", r -> {
                    task.run();
                    return Unit.INSTANCE;
                }),
                () -> true,
                (message, error) -> IOKt.warning(message),
                IOKt::info,
                logErrors,
                logSentData,
                logResponseStatusText);
    }

    /**
     * Adds a custom chart.
     *
     * @param chart The chart to add.
     */
    public void addCustomChart(CustomChart chart) {
        metricsBase.addCustomChart(chart);
    }

    private void appendPlatformData(JsonBuilder builder, Platform platform) {
        builder.appendField("playerAmount", AdapterKt.onlinePlayers().size());
        builder.appendField("javaVersion", System.getProperty("java.version"));
        builder.appendField("osName", System.getProperty("os.name"));
        builder.appendField("osArch", System.getProperty("os.arch"));
        builder.appendField("osVersion", System.getProperty("os.version"));
        builder.appendField("coreCount", Runtime.getRuntime().availableProcessors());
        Map<String, Object> platformData = IOKt.getPlatformData();
        for (Map.Entry<String, Object> entry : platformData.entrySet()) {
            if (entry.getValue() instanceof Integer) {
                if (platform == Platform.NUKKIT) {
                    builder.appendField(entry.getKey().replace("nukkit", "bukkit"), (int) entry.getValue());
                } else {
                    builder.appendField(entry.getKey(), (int) entry.getValue());
                }
            } else {
                if (platform == Platform.NUKKIT) {
                    builder.appendField(entry.getKey().replace("nukkit", "bukkit"), entry.getValue().toString());
                } else {
                    builder.appendField(entry.getKey(), entry.getValue().toString());
                }
            }
        }
    }

    private void appendServiceData(JsonBuilder builder, String version) {
        builder.appendField("pluginVersion", version);
    }
}