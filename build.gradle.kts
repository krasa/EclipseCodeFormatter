@file:Suppress("HardCodedStringLiteral")

import org.jetbrains.changelog.Changelog

fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)
fun Jar.patchManifest() = manifest { attributes("Version" to project.version) }

plugins {
    id("java") // Java support
    alias(libs.plugins.kotlin)
    alias(libs.plugins.gradleIntelliJPlugin) // Gradle IntelliJ Plugin
    alias(libs.plugins.changelog) // Gradle Changelog Plugin
}

group = properties("pluginGroup").get()
version = properties("pluginVersion").get()

// Configure project's dependencies
repositories {
    mavenCentral()
    maven {
        url = uri("https://cache-redirector.jetbrains.com/intellij-dependencies")
    }
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


kotlin {
    jvmToolchain(17)
}

// Configure Gradle IntelliJ Plugin - read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    pluginName = properties("pluginName")
    version = properties("platformVersion")
    type = properties("platformType")

    // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
    plugins = properties("platformPlugins").map { it.split(',').map(String::trim).filter(String::isNotEmpty) }
}


// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    groups.empty()
    repositoryUrl = properties("pluginRepositoryUrl")
}



tasks {
    buildSearchableOptions {
        enabled = false
    }
    compileJava {
        options.encoding = "UTF-8"
    }
    compileTestJava {
        options.encoding = "UTF-8"
    }

    wrapper {
        gradleVersion = properties("gradleVersion").get()
    }

    patchPluginXml {
        version = properties("pluginVersion")
        sinceBuild = properties("pluginSinceBuild")
        untilBuild = properties("pluginUntilBuild")

//        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
//        pluginDescription = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
//            val start = "<!-- Plugin description -->"
//            val end = "<!-- Plugin description end -->"
//
//            with (it.lines()) {
//                if (!containsAll(listOf(start, end))) {
//                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
//                }
//                subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
//            }
//        }

        val changelog = project.changelog // local variable for configuration cache compatibility
        // Get the latest available change notes from the changelog file
        changeNotes = properties("pluginVersion").map { pluginVersion ->
            with(changelog) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        }
    }

    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }


    signPlugin {
        certificateChain = environment("CERTIFICATE_CHAIN")
        privateKey = environment("PRIVATE_KEY")
        password = environment("PRIVATE_KEY_PASSWORD")
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token = environment("PUBLISH_TOKEN")
        // The pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
//        channels = properties("pluginVersion").map { listOf(it.split('-').getOrElse(1) { "default" }.split('.').first()) }
    }
}
