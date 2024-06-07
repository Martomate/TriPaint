package tripaint.view.gui

import tripaint.view.EditMode

import scalafx.geometry.Orientation
import scalafx.scene.layout.TilePane

object ToolBox {
  def create(editModes: Seq[EditMode]): TilePane = {
    val pane = new TilePane
    pane.orientation = Orientation.Vertical
    pane.children = editModes.map(_.toolboxButton)
    pane
  }
}
