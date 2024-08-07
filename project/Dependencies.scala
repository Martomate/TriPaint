import sbt.*

object Dependencies {
  lazy val JavaFxInclude = Seq("base", "controls", "graphics", "media", "swing")
    .map(m => "org.openjfx" % s"javafx-$m" % "21.0.1")
  lazy val JavaFxExclude = Seq("fxml", "web")
    .map(m => "org.openjfx" % s"javafx-$m")

  lazy val MUnit = "org.scalameta" %% "munit" % "1.0.0" % "test"
  lazy val Mockito = "org.scalatestplus" %% "mockito-4-5" % "3.2.12.0" % "test"
}
