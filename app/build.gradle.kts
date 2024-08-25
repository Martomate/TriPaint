plugins {
    scala
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.beryx.runtime") version "1.13.1"
}

application {
    mainClass = "tripaint.TriPaint"
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

    implementation("org.scala-lang:scala3-library_3:3.4.2")
    testImplementation("org.scalameta:munit_3:1.0.0")
    testImplementation("org.scalatestplus:mockito-4-5_3:3.2.12.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testRuntimeOnly("org.junit.platform:junit-platform-runner")
}
