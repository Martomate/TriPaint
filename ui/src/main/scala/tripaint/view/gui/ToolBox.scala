package tripaint.view.gui

import tripaint.util.Resource
import tripaint.view.EditMode

import scalafx.geometry.Orientation
import scalafx.scene.control.{ToggleButton, Tooltip}
import scalafx.scene.image.ImageView
import scalafx.scene.layout.TilePane

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object ToolBox {
  def create(
      editModes: Seq[EditMode],
      currentEditMode: Resource[EditMode],
      setCurrentEditMode: EditMode => Unit
  ): TilePane = {
    val buttons = ArrayBuffer.empty[ToolboxButton]
    val buttonMap = mutable.Map.empty[EditMode, ToolboxButton]

    for m <- editModes yield {
      val b = new ToolboxButton(m, () => setCurrentEditMode(m))
      if m == currentEditMode.value then {
        b.selected = true
      }
      buttons += b
      buttonMap(m) = b
    }

    currentEditMode.onChange((oldMode, newMode) =>
      for b <- buttonMap.get(oldMode) do {
        b.selected = false
      }
      for b <- buttonMap.get(newMode) do {
        b.selected = true
      }
    )

    val pane = new TilePane
    pane.orientation = Orientation.Vertical
    pane.children = buttons
    pane
  }
}

class ToolboxButton(mode: EditMode, onClick: () => Unit)
    extends ToggleButton(null, new ImageView("icons/editmodes/" + mode.imagePath + ".png")) {
  tooltip = new Tooltip(s"${mode.tooltipText}\n(Shortcut: ${mode.shortCut})")
  onAction = _ => onClick()
}
