package tripaint.effects

import tripaint.{Color, HsbColor}
import tripaint.coords.GridCoords
import tripaint.grid.ImageGrid

class RandomNoiseEffect(min: Color, max: Color) extends Effect {
  override def name: String = "Random noise"

  override def action(images: Seq[GridCoords], grid: ImageGrid): Unit = {
    val lo = min.toHsb
    val hi = max.toHsb

    for (imageCoords <- images) {
      val image = grid(imageCoords).storage
      for (coords <- image.allPixels) {
        var hueDiff = hi.h - lo.h
        if hueDiff > 180 then {
          hueDiff -= 360
        } else if hueDiff < -180 then {
          hueDiff += 360
        }
        image.setColor(
          coords,
          HsbColor(
            math.random() * hueDiff + lo.h,
            math.random() * (hi.s - lo.s) + lo.s,
            math.random() * (hi.b - lo.b) + lo.b,
            1
          ).toRgb
        )
      }
    }
  }
}
