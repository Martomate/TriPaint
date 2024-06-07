import Dependencies.*

ThisBuild / name := "TriPaint"
ThisBuild / organization := "com.martomate"
ThisBuild / version := "1.3.4"
ThisBuild / scalaVersion := "3.4.2"

lazy val tripaint = project
  .in(file("."))
  .aggregate(`tripaint-core`, `tripaint-ui`, `tripaint-app`)

lazy val `tripaint-core` = project
  .in(file("core"))
  .settings(
    libraryDependencies ++= Seq(MUnit, Mockito)
  )

lazy val `tripaint-ui` = project
  .in(file("ui"))
  .dependsOn(`tripaint-core`)
  .settings(
    libraryDependencies ++= Seq(MUnit, Mockito),
    libraryDependencies += ScalaFx,
    libraryDependencies ++= JavaFxInclude,
    excludeDependencies ++= JavaFxExclude
  )

lazy val `tripaint-app` = project
  .in(file("app"))
  .dependsOn(`tripaint-core`, `tripaint-ui`)
  .enablePlugins(LauncherJarPlugin)
  .enablePlugins(JlinkPlugin)
  .settings(
    Compile / mainClass := Some("tripaint.TriPaint"),
    Compile / discoveredMainClasses := Seq(),
    Compile / scalacOptions += "-deprecation"
  )
  .settings(
    jlinkIgnoreMissingDependency := JlinkIgnore.only(
      "scalafx" -> "javafx.embed.swing",
      "scalafx.embed.swing" -> "javafx.embed.swing",
      "scalafx" -> "javafx.scene.web",
      "scalafx.scene.web" -> "javafx.scene.web",
      "scala.quoted" -> "scala",
      "scala.quoted.runtime" -> "scala"
    ),
    jlinkOptions ++= Seq(
      "--no-header-files",
      "--no-man-pages",
      "--strip-debug"
    )
  )
  .settings(
    libraryDependencies ++= Seq(MUnit, Mockito),
    libraryDependencies += ScalaFx,
    libraryDependencies ++= JavaFxInclude,
    excludeDependencies ++= JavaFxExclude
  )
