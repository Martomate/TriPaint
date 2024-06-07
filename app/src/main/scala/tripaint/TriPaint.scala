package tripaint

import tripaint.TriPaintModel
import tripaint.control.TriPaintController
import tripaint.view.gui.MainStage

import scalafx.application.{JFXApp3, Platform}
import scalafx.application.JFXApp3.PrimaryStage

object TriPaint extends JFXApp3 {
  override def start(): Unit = {
    val model: TriPaintModel = TriPaintModel.create()
    val controller = new TriPaintController(model, new MainStage(_, _))
    stage = controller.view.asInstanceOf[PrimaryStage]
    Platform.runLater(
      model.imageGrid.setImageSizeIfEmpty(controller.view.askForImageSize().getOrElse(32))
    )
  }
}
