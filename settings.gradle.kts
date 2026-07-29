pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.minecraftforge.net")
        maven("https://repo.spongepowered.org/repository/maven-public/")
        maven("https://maven.kikugie.dev/snapshots")
        maven("https://maven.kikugie.dev/releases")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.3"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

val fabricVersions = providers.gradleProperty("fabric_versions").orNull?.split(", ") ?: emptyList()
val neoforgeVersions = providers.gradleProperty("neoforge_versions").orNull?.split(", ") ?: emptyList()
val forgeVersions = providers.gradleProperty("forge_versions").orNull?.split(", ") ?: emptyList()
val commonVersions = (fabricVersions union neoforgeVersions union forgeVersions).toList()
val dists = mapOf(
    "common" to commonVersions,
    "forge" to forgeVersions,
    "neoforge" to neoforgeVersions,
    "fabric" to fabricVersions
)
val uniqueVersions = dists.values.flatten().distinct()

stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"

    create(rootProject) {
        versions(*uniqueVersions.toTypedArray())

        dists.forEach { (branchName, branchVersions) ->
            branch(branchName) {
                versions(*branchVersions.toTypedArray())
            }
        }
    }
}

rootProject.name = "quick-pack"

