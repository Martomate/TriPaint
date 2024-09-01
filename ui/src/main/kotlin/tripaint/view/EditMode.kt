package tripaint.view

import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination

data class EditMode(
    val imagePath: String,
    val tooltipText: String,
    val shortCut: KeyCodeCombination? = null
) {
    companion object {
        fun all(): List<EditMode> = listOf(Select, Draw, Fill, PickColor, Organize)

        val Select = EditMode("select", "Select", KeyCodeCombination(KeyCode.S))
        val Draw = EditMode("draw", "Draw", KeyCodeCombination(KeyCode.P))
        val Fill = EditMode("fill", "Fill", KeyCodeCombination(KeyCode.F))
        val PickColor = EditMode("pickColor", "Pick Color", KeyCodeCombination(KeyCode.K))
        val Organize = EditMode("organize", "Organize", KeyCodeCombination(KeyCode.O))
    }
}
