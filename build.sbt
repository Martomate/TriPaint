scalaVersion := "2.12.6"

name := "TriPaint"
organization := "com.martomate"
version := "1.2-SNAPSHOT"

enablePlugins(LauncherJarPlugin)

libraryDependencies ++= Seq(
  "org.scalactic" %% "scalactic" % "3.0.5",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "org.scalamock" %% "scalamock" % "4.1.0" % "test",
  "org.scalafx" %% "scalafx" % "8.0.144-R12"
)
