repositories {
    maven{ url = uri("https://www.jitpack.io") }
}

dependencies {
    implementation("com.github.Minestom:Minestom:-SNAPSHOT")
    compileOnly(project(":common"))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}

configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}