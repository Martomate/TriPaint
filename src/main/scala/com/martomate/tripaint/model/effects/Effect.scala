package com.martomate.tripaint.model.effects

import com.martomate.tripaint.model.coords.TriImageCoords
import com.martomate.tripaint.model.grid.ImageGrid

trait Effect {
  def name: String
  def action(images: Seq[TriImageCoords], grid: ImageGrid): Unit
}
