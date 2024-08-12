dependencies {
    compileOnly("ink.ptms.core:v12101:12101:mapped")
}

// 推送第一次会失败, 第二次就成功了, 不知为何
tasks.named("publishMavenPublicationToMavenRepository") {
    dependsOn(tasks.jar)
}