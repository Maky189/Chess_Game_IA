plugins {
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
    maven {
        url = uri("https://repo.jmonkeyengine.org/artifactory/libs-release-local")
    }
    maven {
        url = uri("https://jcenter.bintray.com")
    }
}


application {
    mainClass.set("com.marcos.chess.Main")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.marcos.chess.Main"
    }
}

dependencies {
    val javaFxVersion = "21"
    val os = platform()

    // JavaFX dependencies - include all classifiers
    listOf("base", "graphics", "controls", "fxml", "swing", "media").forEach { module ->
        implementation("org.openjfx:javafx-$module:$javaFxVersion:$os")
    }

    // JMonkeyEngine core dependencies
    implementation("org.jmonkeyengine:jme3-core:3.5.2-stable")
    implementation("org.jmonkeyengine:jme3-desktop:3.5.2-stable")
    implementation("org.jmonkeyengine:jme3-lwjgl:3.5.2-stable")
    implementation("org.jmonkeyengine:jme3-plugins:3.5.2-stable")
    implementation("org.jmonkeyengine:jme3-effects:3.5.2-stable")
    implementation("org.jmonkeyengine:jme3-jogg:3.5.2-stable")
}

fun platform(): String =
    when (System.getProperty("os.name").lowercase()) {
        "mac os x" -> "mac"
        "linux" -> "linux"
        else -> "win"
    }

tasks.shadowJar {
    archiveBaseName.set("ChessGame")
    archiveClassifier.set("")
    archiveVersion.set("1.0.0")

    manifest {
        attributes(
            "Main-Class" to "com.marcos.chess.Main"
        )
    }

    // Merge service files
    mergeServiceFiles()

    // Exclude signature files
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
}

tasks.register("dist") {
    dependsOn("shadowJar")

    doLast {
        val distDir = file("${layout.buildDirectory.get()}/dist")
        distDir.mkdirs()

        // Copy JAR
        copy {
            from("${layout.buildDirectory.get()}/libs/ChessGame-1.0.0.jar")
            into(distDir)
        }

        // Create launcher script
        file("${distDir}/ChessGame.bat").writeText("""
@echo off
start javaw -jar "%~dp0ChessGame-1.0.0.jar"
        """.trimIndent())

        println("")
        println("========================================")
        println("Aplicação empacotada em:")
        println("${distDir.absolutePath}")
        println("")
        println("Para executar: ChessGame.bat")
        println("========================================")
    }
}