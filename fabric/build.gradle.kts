plugins {
    id("net.fabricmc.fabric-loom")
    id("multiloader-loader")
}

version = "fabric-${project.property("mod_version")}+${project.property("minecraft_version")}"

base {
    archivesName = "${project.property("archives_base_name")}"
}

dependencies {
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")

    implementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
}

loom {
    accessWidenerPath = rootDir.resolve("common/src/main/resources/quick-pack.classtweaker")
}

publishMods {
    file.set(tasks.jar.get().archiveFile)

    displayName.set("quick-pack ${version.get()}")
    modLoaders.addAll("fabric", "quilt")
}