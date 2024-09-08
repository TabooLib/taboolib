rootProject.name = "TabooLib"
include("common", "common-env", "common-util", "common-legacy-api", "common-reflex", "common-platform-api")
include(
    // 基础工具
    "module:basic:basic-configuration",
    "module:basic:basic-submit-chain",
    "module:basic:basic-sql",

    // 针对 Bukkit 平台的常规工具
    "module:bukkit:bukkit-hook",
    "module:bukkit:bukkit-ui",
    "module:bukkit:bukkit-ui:bukkit-ui-12100",
    "module:bukkit:bukkit-ui:bukkit-ui-legacy",
    "module:bukkit:bukkit-util",
    "module:bukkit:bukkit-xseries",
    "module:bukkit:bukkit-xseries-item",

    // 针对 Bukkit 平台的 NMS 工具
    "module:bukkit-nms",
    "module:bukkit-nms:nms-ai",
    "module:bukkit-nms:nms-data-serializer",
    "module:bukkit-nms:nms-data-serializer:nms-data-serializer-12100",
    "module:bukkit-nms:nms-data-serializer:nms-data-serializer-legacy",
    "module:bukkit-nms:nms-legacy",
    "module:bukkit-nms:nms-stable",
    "module:bukkit-nms:nms-tag",
    "module:bukkit-nms:nms-tag:nms-tag-12005",
    "module:bukkit-nms:nms-tag:nms-tag-legacy",

    // 针对 Minecraft 的多平台工具
    "module:minecraft:minecraft-chat",
    "module:minecraft:minecraft-command-helper",
    "module:minecraft:minecraft-effect",
    "module:minecraft:minecraft-i18n",
    "module:minecraft:minecraft-metrics",
    "module:minecraft:minecraft-porticus",
)
include(
)
include(
    "platform:platform-application",
    "platform:platform-bukkit",
    "platform:platform-bukkit-impl",
    "platform:platform-bungee",
    "platform:platform-bungee-impl",
    "platform:platform-velocity",
    "platform:platform-velocity-impl",
    "platform:platform-afybroker"
)
