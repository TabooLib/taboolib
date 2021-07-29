dependencies {
    compileOnly("com.google.code.gson:gson:2.8.7")
    compileOnly("com.mongodb:MongoDB:3.12.2:all")
    compileOnly("com.zaxxer:HikariCP:4.0.3")
    compileOnly(project(":common"))
    compileOnly(project(":module-configuration"))
}