package com.martomate.tripaint.model.effects

import com.martomate.tripaint.model.ImageGrid
import com.martomate.tripaint.model.coords.GridCoords

trait Effect {
  def name: String
  def action(images: Seq[GridCoords], grid: ImageGrid): Unit
}
