package tripaint.view.gui

import tripaint.util.Resource
import tripaint.view.EditMode

import javafx.geometry.Orientation
import javafx.scene.control.ToggleButton
import javafx.scene.control.Tooltip
import javafx.scene.image.ImageView
import javafx.scene.layout.TilePane

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
        b.setSelected(true)
      }
      buttons += b
      buttonMap(m) = b
    }

    currentEditMode.onChange((oldMode, newMode) =>
      for b <- buttonMap.get(oldMode) do {
        b.setSelected(false)
      }
      for b <- buttonMap.get(newMode) do {
        b.setSelected(true)
      }
    )

    new TilePane(Orientation.VERTICAL, buttons.toArray*)
  }
}

class ToolboxButton(mode: EditMode, onClick: () => Unit)
    extends ToggleButton(null, new ImageView("icons/editmodes/" + mode.imagePath + ".png")) {
  setTooltip(new Tooltip(s"${mode.tooltipText}\n(Shortcut: ${mode.shortCut})"))
  setOnAction(_ => onClick())
}
