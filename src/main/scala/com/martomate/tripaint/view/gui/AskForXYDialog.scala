package com.martomate.tripaint.view.gui

import com.martomate.tripaint.view.gui.DialogUtils.{getValueFromCustomDialog, makeGridPane}
import scalafx.scene.control.{ButtonType, Label}

import scala.util.Try

object AskForXYDialog:
  def askForXY(title: String, headerText: String): Option[(Int, Int)] =
    val xCoordTF = RestrictedTextField.intTF
    xCoordTF.promptText = "0"
    val yCoordTF = RestrictedTextField.intTF
    yCoordTF.promptText = "0"

    val coordsFromTF = () => {
      val xt = xCoordTF.text()
      val yt = yCoordTF.text()

      for
        xOffset <- Try(if xt != "" then xt.toInt else 0)
        yOffset <- Try(if yt != "" then yt.toInt else 0)
      yield (xOffset, yOffset)
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
      buttons = Seq(ButtonType.OK, ButtonType.Cancel)
    )
