plugins {
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
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

javafx {
    version = "21"
    modules = listOf(
        "javafx.controls",
        "javafx.fxml",
        "javafx.swing",
        "javafx.media"
    )
}

application {
    mainClass.set("com.marcos.chess.Main")
    applicationDefaultJvmArgs = listOf(
        "--module-path", "${System.getProperty("java.home")}/lib",
        "--add-modules", "javafx.controls,javafx.fxml,javafx.swing,javafx.media",
        "--add-exports", "javafx.graphics/com.sun.javafx.sg.prism=ALL-UNNAMED",
        "--add-exports", "javafx.graphics/com.sun.javafx.scene=ALL-UNNAMED"
    )
}

dependencies {
    implementation("org.openjfx:javafx-controls:21:${platform()}")
    implementation("org.openjfx:javafx-fxml:21:${platform()}")
    implementation("org.openjfx:javafx-swing:21:${platform()}")
    implementation("org.openjfx:javafx-media:21:${platform()}")

    // JMonkeyEngine core dependencies
    implementation("org.jmonkeyengine:jme3-core:3.5.2-stable")
    implementation("org.jmonkeyengine:jme3-desktop:3.5.2-stable")
    implementation("org.jmonkeyengine:jme3-lwjgl:3.5.2-stable")
    implementation("org.jmonkeyengine:jme3-plugins:3.5.2-stable")
    implementation("org.jmonkeyengine:jme3-effects:3.5.2-stable")  // Add this line for SSAO filter
    implementation("org.jmonkeyengine:jme3-jogg:3.5.2-stable")  // For OGG audio support
    implementation("org.jmonkeyengine:jme3-plugins:3.5.2-stable")  // For audio plugins
}

fun platform(): String =
    when (System.getProperty("os.name").toLowerCase()) {
        "mac os x" -> "mac"
        "linux" -> "linux"
        else -> "win"
    }