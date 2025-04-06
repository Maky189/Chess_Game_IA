plugins {
    application
    id("org.openjfx.javafxplugin") version "0.0.13"
}

repositories {
    mavenCentral()
}

javafx {
    version = "21" // Use the latest version compatible with your JDK
    modules = listOf("javafx.controls", "javafx.fxml")
}

application {
    mainClass.set("com.marcos.chess.Main")
    applicationDefaultJvmArgs = listOf(
        "--add-modules", "javafx.controls,javafx.fxml"
    )
}

// Add platform-specific JavaFX dependencies dynamically
dependencies {
    implementation("org.openjfx:javafx-controls:21:${platform()}")
    implementation("org.openjfx:javafx-fxml:21:${platform()}")
}

// Dynamically resolve the platform for JavaFX (win, mac, linux)
fun platform(): String =
    when (System.getProperty("os.name").toLowerCase()) {
        "mac os x" -> "mac"
        "linux" -> "linux"
        else -> "win" // Fallback to Windows
    }