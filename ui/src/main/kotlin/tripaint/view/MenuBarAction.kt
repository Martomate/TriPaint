package tripaint.view

import javafx.scene.input.KeyCombination
import tripaint.view.gui.UIAction

data class MenuBarAction(
    val text: String,
    val imagePath: String? = null,
    val accelerator: KeyCombination? = null,
    val action: UIAction? = null
)
