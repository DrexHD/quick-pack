plugins {
    id("dev.kikugie.stonecutter")
    id("org.jetbrains.changelog")
    id("net.fabricmc.fabric-loom") version "1.17-SNAPSHOT" apply false
    id("net.fabricmc.fabric-loom-remap") version "1.17-SNAPSHOT" apply false
}

stonecutter active "26.2"

changelog {
    path = rootProject.file("CHANGELOG.md").path
}

if (project.parent != null) {
    stonecutter {
        tasks {
            order("publishGithub")
            order("publishModrinth")
            order("publishCurseforge")
        }
    }
}

stonecutter parameters {
    replacements {
        string(current.parsed >= "1.21.11") {
            replace("ResourceLocation", "Identifier")
        }

        string(current.parsed >= "26.1") {
            replace("classTweaker v2 named", "classTweaker v2 official")
        }
    }
}