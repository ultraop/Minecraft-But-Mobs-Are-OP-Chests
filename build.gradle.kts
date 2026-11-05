plugins {
    java
}

allprojects {
    group = "in.ultraop.mobchests"
    version = "0.1.0-SNAPSHOT"
    repositories {
        mavenCentral()
        maven(url = "https://repo.papermc.io/repository/maven-public/")
    }
}

subprojects {
    apply(plugin = "java")
    extensions.configure<org.gradle.api.plugins.JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(25))
    }
    tasks.withType<org.gradle.api.tasks.compile.JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(25)
    }
}

tasks.named("build") {
    dependsOn(subprojects.map { it.tasks.named("build") })
}

tasks.named("clean") {
    dependsOn(subprojects.map { it.tasks.named("clean") })
}
