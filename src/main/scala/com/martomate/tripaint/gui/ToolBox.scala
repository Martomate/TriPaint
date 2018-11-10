package com.martomate.tripaint.gui

import com.martomate.tripaint.EditMode
import scalafx.geometry.Orientation
import scalafx.scene.layout.TilePane

class ToolBox extends TilePane {
  orientation = Orientation.Vertical
  children = EditMode.modes.map(_.toolboxButton)
}
