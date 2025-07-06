rootProject.name = "TabooLib"
include("common", "common-env", "common-util", "common-legacy-api", "common-reflex", "common-platform-api")
include(
    // 基础工具
    "module:basic:basic-configuration",
    "module:basic:basic-submit-chain",

    // 针对 Bukkit 平台的常规工具
    "module:bukkit:bukkit-fake-op",
    "module:bukkit:bukkit-hook",
    "module:bukkit:bukkit-navigation",
    "module:bukkit:bukkit-ui",
    "module:bukkit:bukkit-ui:bukkit-ui-12100",
    "module:bukkit:bukkit-ui:bukkit-ui-legacy",
    "module:bukkit:bukkit-util",
    "module:bukkit:bukkit-xseries",

    // 针对 Bukkit 平台的 NMS 工具
    "module:bukkit-nms",
    "module:bukkit-nms:bukkit-nms-ai",
    "module:bukkit-nms:bukkit-nms-data-serializer",
    "module:bukkit-nms:bukkit-nms-data-serializer:nms-data-serializer-12005",
    "module:bukkit-nms:bukkit-nms-data-serializer:nms-data-serializer-legacy",
    "module:bukkit-nms:bukkit-nms-legacy",
    "module:bukkit-nms:bukkit-nms-stable",
    "module:bukkit-nms:bukkit-nms-tag",
    "module:bukkit-nms:bukkit-nms-tag:bukkit-nms-tag-12005",
    "module:bukkit-nms:bukkit-nms-tag:bukkit-nms-tag-12105",
    "module:bukkit-nms:bukkit-nms-tag:bukkit-nms-tag-legacy",

    // 针对 Minecraft 的多平台工具
    "module:minecraft:minecraft-chat",
    "module:minecraft:minecraft-command-helper",
    "module:minecraft:minecraft-effect",
    "module:minecraft:minecraft-i18n",
    "module:minecraft:minecraft-kether",
    "module:minecraft:minecraft-metrics",
    "module:minecraft:minecraft-porticus",

    // 数据库工具
    "module:database",
    "module:database:database-alkaid-redis",
    "module:database:database-ioc",
    "module:database:database-lettuce-redis",
    "module:database:database-player",
    "module:database:database-ptc",
    "module:database:database-ptc-object",
    "module:database:database-player-redis",
    "module:database:database-orm",
    "module:database:database-alkaid-redis-tool",
    "module:database:database-h2",

    // 脚本环境
    "module:script:script-javascript",
    "module:script:script-jexl",

    // 用户空间
    "userspace:geek-tool"
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
