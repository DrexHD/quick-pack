plugins {
	id("multiloader-common")
	id("fabric-loom-compat") apply true
}

dependencies {
	minecraft("com.mojang:minecraft:$minecraftVersion")
	if (stonecutter.eval(minecraftVersion, "<=1.21.11")) {
		mappings(loom.officialMojangMappings())
	}

    compileOnly("org.spongepowered:mixin:0.8.7")

    "io.github.llamalad7:mixinextras-common:0.5.0".let {
        compileOnly(it)
        annotationProcessor(it)
    }

    modCompileOnly("net.fabricmc:fabric-loader:${versionedProp("fabric_loader")}")
}

loom {
	accessWidenerPath = sc.process(
		rootProject.file("common/src/main/resources/quick-pack.classtweaker"),
		"build/processed.classtweaker"
	)
}

val commonJava: Configuration by configurations.creating {
	isCanBeResolved = false
	isCanBeConsumed = true
}

val commonResources: Configuration by configurations.creating {
	isCanBeResolved = false
	isCanBeConsumed = true
}

artifacts {
	afterEvaluate {
		val mainSourceSet = sourceSets.main.get()
		mainSourceSet.java.sourceDirectories.files.forEach {
			add(commonJava.name, it)
		}
		mainSourceSet.resources.sourceDirectories.files.forEach {
			add(commonResources.name, it)
		}
	}
}
