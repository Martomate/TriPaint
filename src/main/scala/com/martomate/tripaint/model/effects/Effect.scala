package com.martomate.tripaint.model.effects

import com.martomate.tripaint.model.coords.TriImageCoords
import com.martomate.tripaint.model.grid.ImageGrid

trait Effect {
  def name: String
  def action(imageCoords: TriImageCoords, grid: ImageGrid): Unit
}
