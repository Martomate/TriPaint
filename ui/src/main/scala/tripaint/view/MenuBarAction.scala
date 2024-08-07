package tripaint.view

import tripaint.view.gui.UIAction

import javafx.scene.input.KeyCombination

case class MenuBarAction(
    text: String,
    imagePath: String = null,
    accelerator: KeyCombination = null,
    action: UIAction = null
)
