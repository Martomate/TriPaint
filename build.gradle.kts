plugins {
    scala
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

allprojects {
    repositories {
        mavenCentral()
    }

    group = "tripaint"
    version = "1.3.4"
}

dependencies {
    implementation("org.scala-lang:scala3-library_3:3.4.2")
    testImplementation("org.scalameta:munit_3:1.0.0")
    testImplementation("org.scalatestplus:mockito-4-5_3:3.2.12.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testRuntimeOnly("org.junit.platform:junit-platform-runner")
}
