plugins {
	`multiloader-loader`
	id("net.neoforged.gradle.userdev") version "7.1.38"
}

dependencies {
	implementation("net.neoforged:neoforge:${versionedProp("neoforge")}")
}

runs {
	named("client") {
		ideRunName = "NeoForge Client (${path})"
	}
	named("server") {
		ideRunName = "NeoForge Server (${path})"
	}

	configureEach {
		modSource(project.sourceSets.main.get())
		workingDirectory("run")
	}
}

publishMods {
    file.set(tasks.jar.get().archiveFile)
}