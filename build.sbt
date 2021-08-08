scalaVersion := "2.13.6"

name := "TriPaint"
organization := "com.martomate"
version := "1.3.1"

enablePlugins(LauncherJarPlugin)

Compile / scalacOptions += "-deprecation"

libraryDependencies ++= Seq(
  "org.scalactic" %% "scalactic" % "3.2.9",
  "org.scalatest" %% "scalatest" % "3.2.9" % "test",
  "org.scalamock" %% "scalamock" % "5.1.0" % "test"
)

// Add dependency on ScalaFX library
libraryDependencies += "org.scalafx" %% "scalafx" % "16.0.0-R24"

lazy val javaFXModules = Seq("base", "controls", "graphics", "media")
libraryDependencies ++= javaFXModules.flatMap { m =>
  Seq(
    "org.openjfx" % s"javafx-$m" % "11" classifier "linux",
    "org.openjfx" % s"javafx-$m" % "11" classifier "mac",
    "org.openjfx" % s"javafx-$m" % "11" classifier "win"
    )
}
