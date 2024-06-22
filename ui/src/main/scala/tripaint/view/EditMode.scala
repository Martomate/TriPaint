package tripaint.view

import javafx.scene.input.{KeyCode, KeyCodeCombination}

class EditMode(
    val imagePath: String,
    val tooltipText: String,
    val shortCut: KeyCodeCombination = null
)

object EditMode {
  def all: Seq[EditMode] = Seq(Select, Draw, Fill, PickColor, Organize)

  val Select = new EditMode("select", "Select", new KeyCodeCombination(KeyCode.S))
  val Draw = new EditMode("draw", "Draw", new KeyCodeCombination(KeyCode.P))
  val Fill = new EditMode("fill", "Fill", new KeyCodeCombination(KeyCode.F))
  val PickColor = new EditMode("pickColor", "Pick Color", new KeyCodeCombination(KeyCode.K))
  val Organize = new EditMode("organize", "Organize", new KeyCodeCombination(KeyCode.O))
}
