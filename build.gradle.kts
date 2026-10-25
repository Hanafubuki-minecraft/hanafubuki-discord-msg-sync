plugins {
    java
}

group = "com.hanafubuki"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.9-R0.1-SNAPSHOT")
    // JDA не вшивается в jar: Paper скачивает его при запуске (plugin.yml → libraries)
    compileOnly("net.dv8tion:JDA:5.3.0") {
        exclude(module = "opus-java")
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.processResources {
    val props = mapOf("version" to project.version)
    inputs.properties(props)
    filesMatching("plugin.yml") { expand(props) }
}

tasks.jar {
    archiveBaseName.set("HanafubukiDiscordSync")
}
