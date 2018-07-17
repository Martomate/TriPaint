package com.martomate.tripaint

import com.martomate.tripaint.TriPaint.makeMenuItem
import com.martomate.tripaint.TriPaint.makeButton
import scalafx.scene.control.{Button, MenuItem}
import scalafx.scene.input.KeyCombination

class MenuBarAction(text: String,
                    imagePath: String,
                    onAction: => Unit,
                    accelerator: KeyCombination) {
  def menuItem: MenuItem = makeMenuItem(text, imagePath, _ => onAction, accelerator)

  def button: Button = makeButton(text, imagePath, _ => onAction)
}

object MenuBarAction {
  def apply(text: String,
            imagePath: String = null,
            accelerator: KeyCombination = null)
           (onAction: => Unit): MenuBarAction =
    new MenuBarAction(text, imagePath, onAction, accelerator)
}