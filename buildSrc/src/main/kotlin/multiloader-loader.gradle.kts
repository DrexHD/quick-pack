plugins {
	id("java")
	id("idea")
    id("multiloader-common")
    id("me.modmuss50.mod-publish-plugin")
}

val commonJava: Configuration by configurations.creating {
    isCanBeResolved = true
}
val commonResources: Configuration by configurations.creating {
    isCanBeResolved = true
}

dependencies {
    val commonPath = ":common"
	compileOnly(project(path = commonPath))
    commonJava(project(path = commonPath, configuration = "commonJava"))
    commonResources(project(path = commonPath, configuration = "commonResources"))
}

tasks {
    compileJava {
        dependsOn(commonJava)
        source(commonJava)
    }

    processResources {
        dependsOn(commonResources)
        from(commonResources)
    }

    publishMods {
        type.set(STABLE)
        displayName.set("quick-pack ${version.get()}")
        changelog.set(providers.environmentVariable("CHANGELOG").orElse("No changelog provided"))

        curseforge {
            accessToken.set(providers.environmentVariable("CURSEFORGE_TOKEN"))
            projectId.set("1380888")
            minecraftVersions.addAll((project.property("curseforge_minecraft_versions") as String).split(", "))
        }

        modrinth {
            accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
            projectId.set("pSISfJ4O")
            minecraftVersions.addAll((project.property("modrinth_minecraft_versions") as String).split(", "))
        }

        github {
            accessToken.set(providers.environmentVariable("GITHUB_TOKEN"))
            repository.set(providers.environmentVariable("GITHUB_REPOSITORY").orElse("DrexHD/quick-pack"))
            commitish.set(providers.environmentVariable("GITHUB_REF_NAME").orElse("main"))
        }
    }
}