scalaVersion := "3.3.1"

name := "TriPaint"
organization := "com.martomate"
version := "1.3.3"

Compile / mainClass := Some("com.martomate.tripaint.TriPaint")
Compile / discoveredMainClasses := Seq()

enablePlugins(LauncherJarPlugin)
enablePlugins(JlinkPlugin)

jlinkIgnoreMissingDependency := JlinkIgnore.only(
  "scalafx" -> "javafx.embed.swing",
  "scalafx.embed.swing" -> "javafx.embed.swing",
  "scalafx" -> "javafx.scene.web",
  "scalafx.scene.web" -> "javafx.scene.web",
  "scala.quoted" -> "scala",
  "scala.quoted.runtime" -> "scala"
)

jlinkOptions ++= Seq(
  "--no-header-files",
  "--no-man-pages",
  "--strip-debug"
)

Compile / scalacOptions += "-deprecation"

libraryDependencies ++= Seq(
  "org.scalameta" %% "munit" % "0.7.29" % "test",
  "org.scalatestplus" %% "mockito-4-5" % "3.2.12.0" % "test"
)

// Add dependency on ScalaFX library
libraryDependencies += "org.scalafx" %% "scalafx" % "21.0.0-R32"
libraryDependencies ++= Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
  .map(m => "org.openjfx" % s"javafx-$m" % "21.0.1")
