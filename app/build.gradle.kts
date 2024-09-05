plugins {
    kotlin("jvm") version "2.0.20"
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.beryx.runtime") version "1.13.1"
}

application {
    mainClass = "tripaint.app.TriPaint"
}

runtime {
    options = listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages")
    launcher {
        noConsole = true
    }

    jpackage {
        installerName = "tripaint"
        imageName = "TriPaint"
        imageOptions.addAll(listOf("--icon", "src/main/resources/icon.icns"))
    }
}

javafx {
    version = "22.0.2"
    modules(
        "javafx.base",
        "javafx.controls",
        "javafx.graphics",
        "javafx.media",
        "javafx.swing",
    )
}

dependencies {
    implementation(project(":tripaint-ui"))
    implementation(project(":tripaint-core"))

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testRuntimeOnly("org.junit.platform:junit-platform-runner")
}

tasks.test {
    useJUnitPlatform()
}
