package tripaint.effects

import tripaint.color.Color
import tripaint.color.HsbColor
import tripaint.coords.GridCoords
import tripaint.grid.ImageGrid

class RandomNoiseEffect(private val min: Color, private val max: Color) : Effect {
    override fun name(): String = "Random noise"

    override fun action(images: List<GridCoords>, grid: ImageGrid) {
        val lo = this.min.toHsb()
        val hi = this.max.toHsb()

        for (imageCoords in images) {
            val image = grid.apply(imageCoords)!!.storage
            for (coords in image.allPixels()) {
                var hueDiff = hi.h - lo.h
                if (hueDiff > 180) {
                    hueDiff -= 360
                } else if (hueDiff < -180) {
                    hueDiff += 360
                }
                image.setColor(
                    coords,
                    HsbColor(
                        Math.random() * hueDiff + lo.h,
                        Math.random() * (hi.s - lo.s) + lo.s,
                        Math.random() * (hi.b - lo.b) + lo.b,
                        1.0
                    ).toRgb()
                )
            }
        }
    }
}
