repositories {
    maven { url = uri("https://nexus.velocitypowered.com/repository/maven-public/") }
    maven { url = uri("https://repo.iroselle.com/repository/velocity-hosted/") } // 防止 velocitypowered repository 炸裂
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.0.1")
    compileOnly(project(":common"))
}