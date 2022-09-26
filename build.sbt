scalaVersion := "3.2.0"

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

jlinkOptions ++= Seq(
  "--no-header-files",
  "--no-man-pages",
  "--strip-debug"
)

Compile / scalacOptions += "-deprecation"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.12" % "test",
  "org.scalatestplus" %% "mockito-4-5" % "3.2.12.0" % "test"
)

// Add dependency on ScalaFX library
libraryDependencies += "org.scalafx" %% "scalafx" % "18.0.2-R29"
