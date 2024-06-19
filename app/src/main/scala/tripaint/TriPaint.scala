package tripaint

import scalafx.application.{JFXApp3, Platform}
import scalafx.application.JFXApp3.PrimaryStage

object TriPaint {
  def main(args: Array[String]): Unit = {
    // By creating a custom main method it becomes possible to perform init code before the app starts
    TriPaint.App.main(args)
  }

  private object App extends JFXApp3 {
    override def start(): Unit = {
      val model: TriPaintModel = TriPaintModel.create()
      val controller = new TriPaintController(model, new MainStage(_, _))
      stage = controller.view.asInstanceOf[PrimaryStage]
      Platform.runLater(
        model.imageGrid.setImageSizeIfEmpty(controller.view.askForImageSize().getOrElse(32))
      )
    }
  }
}
