package com.martomate.tripaint.model.effects

import com.martomate.tripaint.model.coords.TriangleCoords
import com.martomate.tripaint.model.storage.ImageStorage
import scalafx.scene.paint.Color

class BlurEffect(radius: Int) extends LocalEffect {
  def name: String = "Blur"

  private val radiusSq = radius * radius

  override def predicate(image: ImageStorage, here: TriangleCoords)(coords: TriangleCoords): Boolean = {
    coords.distanceSq(here) <= radiusSq * 1.5
  }

  override def weightedColor(image: ImageStorage, here: TriangleCoords)(coords: TriangleCoords): (Double, Color) = {
    (math.exp(-2 * coords.distanceSq(here) / radiusSq), image(coords))
  }
}
