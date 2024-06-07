package tripaint.model.effects

import tripaint.Color
import tripaint.ScalaFxExt.{*, given}
import tripaint.coords.GridCoords
import tripaint.model.ImageGrid

import scalafx.scene.paint.Color as FXColor

import scala.language.implicitConversions

class RandomNoiseEffect(min: Color, max: Color) extends Effect {
  override def name: String = "Random noise"

  override def action(images: Seq[GridCoords], grid: ImageGrid): Unit = {
    val lo = min.toFXColor
    val hi = max.toFXColor

    for (imageCoords <- images) {
      val image = grid(imageCoords).get.storage
      for (coords <- image.allPixels) {
        var hueDiff = hi.hue - lo.hue
        if hueDiff > 180 then {
          hueDiff -= 360
        } else if hueDiff < -180 then {
          hueDiff += 360
        }
        image.setColor(
          coords,
          FXColor.hsb(
            math.random() * hueDiff + lo.hue,
            math.random() * (hi.saturation - lo.saturation) + lo.saturation,
            math.random() * (hi.brightness - lo.brightness) + lo.brightness,
            1
          )
        )
      }
    }
  }
}
