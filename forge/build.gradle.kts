plugins {
    id("net.minecraftforge.gradle") version "7.0.31"
    id("multiloader-loader")
}

repositories {
    minecraft.mavenizer(this)
    maven(fg.forgeMaven)
    maven(fg.minecraftLibsMaven)
}

version = "forge-${project.property("mod_version")}+${project.property("minecraft_version")}"

base {
    archivesName = "${project.property("archives_base_name")}"
}

dependencies {
    implementation(minecraft.dependency("net.minecraftforge:forge:${project.property("minecraft_version")}-${project.property("forge_version")}"))

    compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:0.5.3")!!)
    implementation("io.github.llamalad7:mixinextras-forge:0.5.3")
}


tasks.jar {
    manifest.attributes(mapOf(
        "MixinConfigs" to "quick-pack.mixins.json"
    ))
}

publishMods {
    file.set(tasks.jar.get().archiveFile)

    displayName.set("quick-pack ${version.get()}")
    modLoaders.addAll("forge")
}