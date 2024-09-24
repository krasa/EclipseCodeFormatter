plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.0"
}

tasks {
    compileTestJava {
        options.encoding = "UTF-8"
    }
}

group = "EclipseCodeFormatter"
version = "23.4.223.000.0-Eclipse_2024-09"

tasks {
    patchPluginXml {
        sinceBuild.set("223.0")
        untilBuild.set("")
        changeNotes.set(
            buildString {
                append("- 2024.1 EAP compatibility").append("<br>")
            }
        )
    }

    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }


    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }

    buildSearchableOptions {
        enabled = false
    }
}

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
//    version.set("2023.1")
    version.set("LATEST-EAP-SNAPSHOT")
//    version.set("2022.3")
    type.set("IC") // Target IDE Platform
    plugins.set(listOf("java"))
}


dependencies {

// https://mvnrepository.com/artifact/org.easymock/easymock
    testImplementation("org.easymock:easymock:5.1.0")

    implementation(
        files(
            "lib/eclipse/adapter.jar",
            "lib/eclipse/eclipse.jar",
            "lib/bare-bones-browser-launch-3.1.jar",
            "lib/batik-ext-1.11.jar"
        )
    )


// https://mvnrepository.com/artifact/commons-beanutils/commons-beanutils
    implementation("commons-beanutils:commons-beanutils:1.9.4")

// https://mvnrepository.com/artifact/commons-io/commons-io
    implementation("commons-io:commons-io:2.11.0")

    // https://mvnrepository.com/artifact/org.apache.commons/commons-lang3
    implementation("org.apache.commons:commons-lang3:3.14.0")

}

