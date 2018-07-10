package tripaint

import scalafx.beans.property.ReadOnlyObjectWrapper
import scalafx.scene.control.ToggleButton
import scalafx.scene.control.Tooltip
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import scalafx.scene.image.Image
import scalafx.scene.image.ImageView

class EditMode(imagePath: String, val tooltipText: String, val shortCut: KeyCodeCombination = null) {
  val toolboxButton = {
    val t = new ToggleButton(null, new ImageView("icons/editmode/" + imagePath + ".png"))
    t.tooltip = new Tooltip(s"$tooltipText\n(Shortcut: $shortCut)")
    t.onAction = _ => select
    t
  }
  
  EditMode._modes :+= this
  
  def select: Unit = {
    EditMode.deselectAll
    toolboxButton.selected = true
    
    EditMode.currentMode = this
  }
}

object EditMode {
  private var _modes = Vector.empty[EditMode]
  def modes = _modes
  
  private var _currentMode: ReadOnlyObjectWrapper[EditMode] = new ReadOnlyObjectWrapper(null, null)
  private def currentMode_=(mode: EditMode) = _currentMode.value = mode
  def currentMode: EditMode = _currentMode.value
  def currentModeProperty = _currentMode.readOnlyProperty
  
  val Select = new EditMode("select", "Select", new KeyCodeCombination(KeyCode.S))
  val Draw = new EditMode("draw", "Draw", new KeyCodeCombination(KeyCode.P))
  val Fill = new EditMode("fill", "Fill", new KeyCodeCombination(KeyCode.F))
  val PickColor = new EditMode("pickColor", "Pick Color", new KeyCodeCombination(KeyCode.K))
  val Orgianize = new EditMode("organize", "Organize", new KeyCodeCombination(KeyCode.O))
  
  Draw.select
  
  private def deselectAll: Unit = EditMode.modes.foreach(_.toolboxButton.selected = false)
}