dependencies {
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("com.google.guava:guava:21.0")
    implementation("org.apache.commons:commons-lang3:3.5")
    implementation(project(":common-util"))
}

//shrinking {
//    shadow = true
//}

tasks {
    build {
        dependsOn("shadowJar")
    }
}