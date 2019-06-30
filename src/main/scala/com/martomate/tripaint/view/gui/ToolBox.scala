package com.martomate.tripaint.view.gui

import com.martomate.tripaint.view.EditMode
import scalafx.geometry.Orientation
import scalafx.scene.layout.TilePane

class ToolBox extends TilePane {
  orientation = Orientation.Vertical
  children = EditMode.modes.map(_.toolboxButton)
}
