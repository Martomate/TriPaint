scalaVersion := "2.13.2"

name := "TriPaint"
organization := "com.martomate"
version := "1.2"

enablePlugins(LauncherJarPlugin)

scalacOptions in Compile += "-deprecation"

libraryDependencies ++= Seq(
  "org.scalactic" %% "scalactic" % "3.1.2",
  "org.scalatest" %% "scalatest" % "3.1.2" % "test",
  "org.scalamock" %% "scalamock" % "4.4.0" % "test"
)

// Add dependency on ScalaFX library
libraryDependencies += "org.scalafx" %% "scalafx" % "14-R19"

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
