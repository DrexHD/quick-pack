buildscript {
    repositories {
        maven("https://maven.minecraftforge.net")
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath(group= "net.minecraftforge.gradle", name="ForgeGradle", version = "5.1.+")
        classpath("org.spongepowered:mixingradle:0.7.+")
    }
}

plugins {
    id("net.minecraftforge.gradle") version "6.0.36"
    id("org.spongepowered.mixin") version "0.7.+"
    id("multiloader-loader")
}

version = "forge-${project.property("mod_version")}+${project.property("minecraft_version")}"

base {
    archivesName = "${project.property("archives_base_name")}"
}

minecraft {
    mappingChannel = "official"
    mappingVersion = "${project.property("minecraft_version")}"

    copyIdeResources = true
}

dependencies {
    minecraft("net.minecraftforge:forge:${project.property("minecraft_version")}-${project.property("forge_version")}")

    compileOnly(annotationProcessor("org.spongepowered:mixin:0.8.7:processor")!!)
    compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:0.5.0")!!)
    implementation(jarJar("io.github.llamalad7:mixinextras-forge:0.5.0")!!).apply {
        jarJar.ranged(this, "[0.5.0,)")
    }
}

mixin {
    add(sourceSets.main.get(), "quick-pack.refmap.json")
    config("quick-pack.mixins.json")
}

tasks.jar.configure {
    finalizedBy("reobfJar")
}

tasks.jarJar.configure {
    finalizedBy("reobfJarJar")
}

publishMods {
    file.set(tasks.jarJar.get().archiveFile)

    displayName.set("quick-pack ${version.get()}")
    modLoaders.addAll("forge")
}