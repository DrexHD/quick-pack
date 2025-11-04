import kotlin.collections.addAll

plugins {
    id("fabric-loom")
    id("multiloader-loader")
}

base {
    archivesName = "${project.property("archives_base_name")}-fabric"
}

dependencies {
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    mappings(loom.officialMojangMappings())

    modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
}

publishMods {
    file.set(tasks.remapJar.get().archiveFile)

    displayName.set("quick-pack fabric $version")
    modLoaders.addAll("fabric", "quilt")
}