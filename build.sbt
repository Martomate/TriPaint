scalaVersion := "2.13.6"

name := "TriPaint"
organization := "com.martomate"
version := "1.3.1"

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
  "org.scalatest" %% "scalatest" % "3.2.9" % "test",
  "org.scalamock" %% "scalamock" % "5.1.0" % "test"
)

// Add dependency on ScalaFX library
libraryDependencies += "org.scalafx" %% "scalafx" % "16.0.0-R24"

lazy val javaFXModules = Seq("base", "controls", "graphics", "media")
libraryDependencies ++= javaFXModules.map { m =>
  "org.openjfx" % s"javafx-$m" % "16"
}
