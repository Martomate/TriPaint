package tripaint.view.gui

import tripaint.view.gui.DialogUtils.{getValueFromCustomDialog, makeGridPane}

import javafx.scene.control.ButtonType
import javafx.scene.control.Label

import scala.util.Try

object AskForXYDialog {
  def askForXY(title: String, headerText: String): Option[(Int, Int)] = {
    val xCoordTF = RestrictedTextField.intTF
    xCoordTF.setPromptText("0")
    val yCoordTF = RestrictedTextField.intTF
    yCoordTF.setPromptText("0")

    val coordsFromTF = () => {
      val xt = xCoordTF.getText()
      val yt = yCoordTF.getText()

      for {
        xOffset <- Try(if xt != "" then xt.toInt else 0)
        yOffset <- Try(if yt != "" then yt.toInt else 0)
      } yield (xOffset, yOffset)
    }

    getValueFromCustomDialog[(Int, Int)](
      title = title,
      headerText = headerText,
      content = Seq(
        makeGridPane(
          Seq(
            Seq(new Label("X coordinate:"), xCoordTF),
            Seq(new Label("Y coordinate:"), yCoordTF)
          )
        )
      ),
      resultConverter = {
        case ButtonType.OK => coordsFromTF().getOrElse(null)
        case _             => null
      },
      buttons = Seq(ButtonType.OK, ButtonType.CANCEL)
    )
  }
}
