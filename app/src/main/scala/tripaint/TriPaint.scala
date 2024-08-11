package tripaint

import javafx.application.{Application, Platform}
import javafx.stage.Stage

object TriPaint {
  def main(args: Array[String]): Unit = {
    // By creating a custom main method it becomes possible to perform init code before the app starts
    Application.launch(classOf[App], args*)
  }

  class App extends Application {
    override def start(stage: Stage): Unit = {
      val model: TriPaintModel = TriPaintModel.create()
      val controller = new TriPaintController(model, new MainStage(_, _, stage))

      Platform.runLater(() =>
        model.imageGrid.setImageSizeIfEmpty(controller.view.askForImageSize().getOrElse(32))
      )

      stage.show()
    }
  }
}
