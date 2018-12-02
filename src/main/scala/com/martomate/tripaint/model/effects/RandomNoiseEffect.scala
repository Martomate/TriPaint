package com.martomate.tripaint.model.effects

import com.martomate.tripaint.model.storage.ImageStorage
import scalafx.scene.paint.Color

class RandomNoiseEffect(min: Color, max: Color) extends Effect {
  override def name: String = "Random noise"

  override def action(image: ImageStorage): Unit = {
    for (coords <- image.allPixels) {
      image(coords) = Color.hsb(
        math.random * (max.hue - min.hue) + min.hue,
        math.random * (max.saturation - min.saturation) + min.saturation,
        math.random * (max.brightness - min.brightness) + min.brightness,
        1
      )
    }
  }
}
