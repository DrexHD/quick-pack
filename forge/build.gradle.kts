import net.minecraftforge.renamer.gradle.RenameJar

plugins {
	`multiloader-loader`
	id("net.minecraftforge.gradle") version "[7.0.17,8)"
	id("net.minecraftforge.jarjar") version "0.2.3"
	id("net.minecraftforge.renamer") version "1.1.0"
}

repositories {
	mavenCentral()
	maven(fg.forgeMaven)
	maven(fg.minecraftLibsMaven)
	minecraft.mavenizer(this)
}

jarJar {
	register("jarJar")
}

dependencies {
	implementation(minecraft.dependency("net.minecraftforge:forge:${versionedProp("forge")}"))

	if (stonecutter.eval(project.minecraftVersion, "<=1.20.6")) {
		annotationProcessor("org.spongepowered:mixin:0.8.7:processor")
	}
	implementation("org.spongepowered:mixin:0.8.7")

	compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:0.5.3")!!)
	implementation("io.github.llamalad7:mixinextras-forge:0.5.3")

	// 1.21.10 bundles mixin extras
	if (stonecutter.eval(minecraftVersion, "<1.21.10")) {
		add("jarJar", "io.github.llamalad7:mixinextras-forge:0.5.3")
	}
}

minecraft {
	accessTransformer += files(rootProject.file("forge/src/main/resources/META-INF/accesstransformer.cfg"))
	if (stonecutter.eval(minecraftVersion, "<=1.21.11")) {
		mappings("official", minecraftVersion)
	}

	runs {
		configureEach {
			workingDir = layout.projectDirectory.dir("run")
		}

		register("client")

		register("server") {
			args("--nogui")
		}

	}
}

if (stonecutter.eval(minecraftVersion, "<1.20.6")) {
	renamer {
		mappings(minecraft.dependency.toSrg)

		enableMixinRefmaps {
			config("quick-pack.mixins.json")
			source(project.sourceSets.main.get()) {
				refMap = "quick_pack.refmap.json"
			}
			jar(tasks.named<Jar>("jarJar"))
		}
		classes(tasks.named<Jar>("jarJar")) {
			mappings(renamer.mixin.generatedMappings)
			archiveClassifier.set("srg")
		}
	}
}

tasks.jar {
	manifest {
		attributes(
			"MixinConfigs" to "quick-pack.mixins.json"
		)
	}
}

publishMods {
	if (stonecutter.eval(minecraftVersion, ">=1.20.6")) {
		file.set(tasks.named<Jar>("jarJar").flatMap { it.archiveFile })
	} else {
		file.set(tasks.named<RenameJar>("renameJarJar").flatMap { it.output })
	}
}