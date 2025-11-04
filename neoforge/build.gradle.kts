plugins {
    id("multiloader-loader")
    id("net.neoforged.gradle.userdev") version "7.0.192"
}

base {
    archivesName = "${project.property("archives_base_name")}-neoforge"
}

dependencies {
    implementation("net.neoforged:neoforge:${project.property("neoforge_version")}")
}

publishMods {
    file.set(tasks.jarJar.get().archiveFile)

    displayName.set("quick-pack neoforge $version")
    modLoaders.addAll("neoforge")
}