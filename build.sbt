scalaVersion := "3.1.2"

name := "TriPaint"
organization := "com.martomate"
version := "1.3.2"

Compile / mainClass := Some("com.martomate.tripaint.TriPaint")
Compile / discoveredMainClasses := Seq()

enablePlugins(LauncherJarPlugin)
enablePlugins(JlinkPlugin)

jlinkIgnoreMissingDependency := JlinkIgnore.only(
  "scalafx" -> "javafx.embed.swing",
  "scalafx.embed.swing" -> "javafx.embed.swing",
  "scalafx" -> "javafx.scene.web",
  "scalafx.scene.web" -> "javafx.scene.web"
)

jlinkModules += "jdk.unsupported"

jlinkOptions ++= Seq(
  "--no-header-files",
  "--no-man-pages",
  "--strip-debug"
)

Compile / scalacOptions += "-deprecation"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.11" % "test",
  "org.scalatestplus" %% "mockito-4-2" % "3.2.11.0" % "test"
)

// Add dependency on ScalaFX library
libraryDependencies += "org.scalafx" %% "scalafx" % "17.0.1-R26"

lazy val javaFXModules = Seq("base", "controls", "graphics", "media")
libraryDependencies ++= javaFXModules.map { m =>
  "org.openjfx" % s"javafx-$m" % "17.0.2"
}
