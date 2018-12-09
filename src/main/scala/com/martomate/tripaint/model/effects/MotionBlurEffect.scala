package com.martomate.tripaint.model.effects

import com.martomate.tripaint.model.coords.{PixelCoords, TriangleCoords}
import com.martomate.tripaint.model.storage.ImageStorage
import scalafx.scene.paint.Color

class MotionBlurEffect(radius: Int) extends LocalEffect {
  def name: String = "Motion blur"

  private val radiusSq = radius * radius

  override def predicate(image: ImageStorage, here: PixelCoords)(coords: PixelCoords, color: Color): Boolean = {
    coords.image == here.image && here.pix.y == coords.pix.y && math.pow(here.pix.x - coords.pix.x, 2) <= radiusSq * 1.5
  }

  override def weightedColor(image: ImageStorage, here: PixelCoords)(coords: PixelCoords): (Double, Color) = {
    if (coords.image == here.image) (math.exp(-2 * math.pow(here.pix.x - coords.pix.x, 2) / radiusSq), image(coords.pix)) else (0, Color.Black)
  }
}
