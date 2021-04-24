scalaVersion := "2.13.2"

name := "TriPaint"
organization := "com.martomate"
version := "1.3"

enablePlugins(LauncherJarPlugin)

scalacOptions in Compile += "-deprecation"

libraryDependencies ++= Seq(
  "org.scalactic" %% "scalactic" % "3.2.2",
  "org.scalatest" %% "scalatest" % "3.2.2" % "test",
  "org.scalamock" %% "scalamock" % "5.0.0" % "test"
)

// Add dependency on ScalaFX library
libraryDependencies += "org.scalafx" %% "scalafx" % "14-R19"

lazy val javaFXModules = Seq("base", "controls", "graphics", "media")
libraryDependencies ++= javaFXModules.flatMap { m =>
  Seq(
    "org.openjfx" % s"javafx-$m" % "11" classifier "linux",
    "org.openjfx" % s"javafx-$m" % "11" classifier "mac",
    "org.openjfx" % s"javafx-$m" % "11" classifier "win"
    )
}
