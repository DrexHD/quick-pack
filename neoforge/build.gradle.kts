plugins {
    id("multiloader-loader")
    id("net.neoforged.gradle.userdev") version "7.1.4"
}

version = "neoforge-${project.property("mod_version")}+${project.property("minecraft_version")}"

base {
    archivesName = "${project.property("archives_base_name")}"
}

dependencies {
    implementation("net.neoforged:neoforge:${project.property("neoforge_version")}")
}

publishMods {
    file.set(tasks.jar.get().archiveFile)

    displayName.set("quick-pack ${version.get()}")
    modLoaders.addAll("neoforge")
}