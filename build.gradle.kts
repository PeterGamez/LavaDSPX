import org.ajoberstar.grgit.Grgit

plugins {
    java
    `maven-publish`
    id("org.ajoberstar.grgit") version "5.2.0"
    alias(libs.plugins.lavalink)
}

val (gitVersion, release) = versionFromGit()
logger.lifecycle("Version: $gitVersion (release: $release)")

group = "me.devoxin.lavadspx.plugin"
version = gitVersion.removeSuffix("-SNAPSHOT")

lavalinkPlugin {
    name = "lavadspx-plugin"
    apiVersion = libs.versions.lavalink.api
    serverVersion = libs.versions.lavalink.server
    configurePublishing = false
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }
}

repositories {
    maven {
        setUrl("https://jitpack.io")
    }
}

dependencies {
    implementation("com.github.Devoxin:LavaDSPX:2.0.1")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

@SuppressWarnings("GrMethodMayBeStatic")
fun versionFromGit(): Pair<String, Boolean> {
    Grgit.open(mapOf("currentDir" to project.rootDir)).use { git ->
        val headTag = git.tag
            .list()
            .find { it.commit.id == git.head().id }

        val clean = git.status().isClean || System.getenv("CI") != null
        if (!clean) {
            logger.lifecycle("Git state is dirty, version is a snapshot.")
        }

        return if (headTag != null && clean) {
            headTag.name to true
        } else {
            "${git.head().id}-SNAPSHOT" to false
        }
    }
}

