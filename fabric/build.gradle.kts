import kotlin.collections.addAll

plugins {
    id("fabric-loom")
    id("multiloader-loader")
}

version = "fabric-${project.property("mod_version")}+${project.property("minecraft_version")}"

base {
    archivesName = "${project.property("archives_base_name")}"
}

dependencies {
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    mappings(loom.officialMojangMappings())

    modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
}

publishMods {
    file.set(tasks.remapJar.get().archiveFile)

    displayName.set("quick-pack ${version.get()}")
    modLoaders.addAll("fabric", "quilt")
}