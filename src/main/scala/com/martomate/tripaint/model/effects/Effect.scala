package com.martomate.tripaint.model.effects

import com.martomate.tripaint.model.ImageGrid
import com.martomate.tripaint.model.coords.TriImageCoords

trait Effect {
  def name: String
  def action(images: Seq[TriImageCoords], grid: ImageGrid): Unit
}
