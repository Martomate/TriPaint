package com.martomate.tripaint.view

import com.martomate.tripaint.view.gui.UIAction
import javafx.event.{ActionEvent, EventHandler}
import scalafx.scene.control.{Button, MenuItem, Tooltip}
import scalafx.scene.image.ImageView
import scalafx.scene.input.KeyCombination

case class MenuBarAction(
    text: String,
    imagePath: String = null,
    accelerator: KeyCombination = null,
    action: UIAction = null
)
