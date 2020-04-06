scalaVersion := "2.12.11"

name := "TriPaint"
organization := "com.martomate"
version := "1.2"

enablePlugins(LauncherJarPlugin)

libraryDependencies ++= Seq(
  "org.scalactic" %% "scalactic" % "3.0.5",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "org.scalamock" %% "scalamock" % "4.1.0" % "test"
)

// Add dependency on ScalaFX library
libraryDependencies += "org.scalafx" %% "scalafx" % "11-R16"

// Determine OS version of JavaFX binaries
lazy val osName = System.getProperty("os.name") match {
  case n if n.startsWith("Linux")   => "linux"
  case n if n.startsWith("Mac")     => "mac"
  case n if n.startsWith("Windows") => "win"
  case _ => throw new Exception("Unknown platform!")
}

lazy val javaFXModules = Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
libraryDependencies ++= javaFXModules.map { m =>
  "org.openjfx" % s"javafx-$m" % "11" classifier osName
}
