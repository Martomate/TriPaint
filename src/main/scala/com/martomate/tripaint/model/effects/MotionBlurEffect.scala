package com.martomate.tripaint.model.effects

import com.martomate.tripaint.model.coords.TriangleCoords
import com.martomate.tripaint.model.storage.ImageStorage
import scalafx.scene.paint.Color

class MotionBlurEffect(radius: Int) extends LocalEffect {
  def name: String = "Motion blur"

  private val radiusSq = radius * radius

  override def predicate(image: ImageStorage, here: TriangleCoords)(coords: TriangleCoords): Boolean = {
    here.y == coords.y && math.pow(here.x - coords.x, 2) <= radiusSq * 1.5
  }

  override def weightedColor(image: ImageStorage, here: TriangleCoords)(coords: TriangleCoords): (Double, Color) = {
    (math.exp(-2 * math.pow(here.x - coords.x, 2) / radiusSq), image(coords))
  }
}
