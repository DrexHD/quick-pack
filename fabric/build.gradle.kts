plugins {
    id("fabric-loom-compat")
    `multiloader-loader`
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    if (stonecutter.eval(minecraftVersion, "<=1.21.11")) {
        mappings(loom.officialMojangMappings())
    }

    modImplementation("net.fabricmc:fabric-loader:${versionedProp("fabric_loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${versionedProp("fabric_api")}")
}

loom {
    accessWidenerPath = sc.process(
        rootProject.file("common/src/main/resources/quick-pack.classtweaker"),
        "build/processed.classtweaker"
    )

    runs {
        getByName("client") {
            client()
            configName = "Fabric Client"
            ideConfigGenerated(true)
        }
        getByName("server") {
            server()
            configName = "Fabric Server"
            ideConfigGenerated(true)
        }
    }
}

publishMods {
    file.set(the<FabricLoomCompatPlugin.FabricExtensions>().modJar.flatMap { it.archiveFile })
}