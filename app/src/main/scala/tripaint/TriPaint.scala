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
      val controller = new TriPaintController(model, new MainStage(_, _))
      val s = controller.view.asInstanceOf[Stage]

      // TODO: send stage to the UI instead of copying over the data
      stage.setTitle(s.getTitle)
      stage.setScene(s.getScene)
      stage.setOnCloseRequest(s.getOnCloseRequest)

      Platform.runLater(() =>
        model.imageGrid.setImageSizeIfEmpty(controller.view.askForImageSize().getOrElse(32))
      )

      stage.show()
    }
  }
}
