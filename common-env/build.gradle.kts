dependencies {
    compileOnly(project(":common"))
    compileOnly("org.ow2.asm:asm:9.8")
    compileOnly("org.ow2.asm:asm-util:9.8")
    compileOnly("org.ow2.asm:asm-commons:9.8")
    compileOnly("me.lucko:jar-relocator:1.7")
    // 自 1.17 开始使用服务端提供的 Aether 下载依赖
    compileOnly("org.apache.maven.resolver:maven-resolver-api:1.9.18")
    compileOnly("org.apache.maven.resolver:maven-resolver-util:1.9.18")
    compileOnly("org.apache.maven.resolver:maven-resolver-impl:1.9.18")
    compileOnly("org.apache.maven.resolver:maven-resolver-connector-basic:1.9.18")
    compileOnly("org.apache.maven.resolver:maven-resolver-transport-wagon:1.9.18")
    compileOnly("org.apache.maven.resolver:maven-resolver-transport-http:1.9.18")
    compileOnly("org.apache.maven:maven-resolver-provider:3.9.6")
}