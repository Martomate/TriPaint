package com.martomate.tripaint.model.effects

import com.martomate.tripaint.model.{Color, ImageGrid}
import com.martomate.tripaint.model.coords.GridCoords

import scalafx.scene.paint.{Color => FXColor}

class RandomNoiseEffect(min: Color, max: Color) extends Effect {
  override def name: String = "Random noise"

  override def action(images: Seq[GridCoords], grid: ImageGrid): Unit = {
    val lo = min.toFXColor
    val hi = max.toFXColor

    for (imageCoords <- images) {
      val image = grid(imageCoords).get.storage
      for (coords <- image.allPixels) {
        image.setColor(
          coords,
          FXColor.hsb(
            math.random() * (hi.hue - lo.hue) + lo.hue,
            math.random() * (hi.saturation - lo.saturation) + lo.saturation,
            math.random() * (hi.brightness - lo.brightness) + lo.brightness,
            1
          )
        )
      }
    }
  }
}
