repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    // 这是假的 1.21 吧, 我服了
    // 明明 1.21 里 InventoryView 是个 interface, 这里怎么不是??
    // compileOnly("ink.ptms.core:v12100:12100:mapped")
    compileOnly("org.spigotmc:spigot-api:1.21-R0.1-SNAPSHOT")
}

// 推送第一次会失败, 第二次就成功了, 不知为何
tasks.named("publishMavenPublicationToMavenRepository") {
    dependsOn(tasks.jar)
}