package com.martomate.tripaint.view

import javafx.event.{ActionEvent, EventHandler}
import scalafx.scene.control.{Button, MenuItem, Tooltip}
import scalafx.scene.image.ImageView
import scalafx.scene.input.KeyCombination

class MenuBarAction(text: String,
                    imagePath: String,
                    onAction: => Unit,
                    accelerator: KeyCombination) {
  def menuItem: MenuItem = makeMenuItem(text, imagePath, _ => onAction, accelerator)

  def button: Button = makeButton(text, imagePath, _ => onAction)

  private def makeMenuItem(text: String, imagePath: String = null, onAction: EventHandler[ActionEvent], accelerator: KeyCombination = null): MenuItem = {
    val item = if (imagePath == null) new MenuItem(text) else new MenuItem(text, new ImageView("icons/" + imagePath + ".png"))
    item.onAction = onAction
    if (accelerator != null) item.accelerator = accelerator
    item
  }

  private def makeButton(text: String, imagePath: String = null, onAction: EventHandler[ActionEvent]): Button = {
    val item = if (imagePath == null) new Button(text) else new Button(null, new ImageView("icons/" + imagePath + ".png"))
    item.onAction = onAction
    item.tooltip = new Tooltip(text)
    item
  }
}

object MenuBarAction {
  def apply(text: String,
            imagePath: String = null,
            accelerator: KeyCombination = null)
           (onAction: => Unit): MenuBarAction =
    new MenuBarAction(text, imagePath, onAction, accelerator)
}