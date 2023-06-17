package com.martomate.tripaint.model.effects

import com.martomate.tripaint.model.ImageGrid
import com.martomate.tripaint.model.coords.GridCoords
import scalafx.scene.paint.Color

class RandomNoiseEffect(min: Color, max: Color) extends Effect {
  override def name: String = "Random noise"

  override def action(images: Seq[GridCoords], grid: ImageGrid): Unit = {
    for (imageCoords <- images) {
      val image = grid(imageCoords).get.storage
      for (coords <- image.allPixels) {
        image.setColor(
          coords,
          Color.hsb(
            math.random() * (max.hue - min.hue) + min.hue,
            math.random() * (max.saturation - min.saturation) + min.saturation,
            math.random() * (max.brightness - min.brightness) + min.brightness,
            1
          )
        )
      }
    }
  }
}
