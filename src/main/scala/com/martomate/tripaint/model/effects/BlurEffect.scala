package com.martomate.tripaint.model.effects

import com.martomate.tripaint.model.coords.PixelCoords
import com.martomate.tripaint.model.storage.ImageStorage
import scalafx.scene.paint.Color

class BlurEffect(radius: Int) extends LocalEffect {
  def name: String = "Blur"

  private val radiusSq = radius * radius

  override def predicate(image: ImageStorage, here: PixelCoords)(coords: PixelCoords, color: Color): Boolean = {
    coords.image == here.image && coords.pix.distanceSq(here.pix) <= radiusSq * 1.5
  }

  override def weightedColor(image: ImageStorage, here: PixelCoords)(coords: PixelCoords): (Double, Color) = {
    if (coords.image == here.image) (math.exp(-2 * coords.pix.distanceSq(here.pix) / radiusSq), image(coords.pix)) else (0, Color.Black)
  }
}
