package tripaint.effects

import tripaint.ColorLookup
import tripaint.color.Color
import tripaint.coords.GlobalPixCoords

class BlurEffect(radius: Int) extends LocalEffect {
  def name: String = "Blur"

  private val radiusSq = radius.toDouble * radius

  override protected def predicate(
      image: ColorLookup,
      here: GlobalPixCoords
  )(coords: GlobalPixCoords, color: Color): Boolean = {
    coords.distanceSq(here) <= radiusSq * 1.5
  }

  override protected def weightedColor(image: ColorLookup, here: GlobalPixCoords)(
      coords: GlobalPixCoords
  ): (Double, Color) = {
    (math.exp(-2 * coords.distanceSq(here) / radiusSq), image.lookup(coords).get)
  }
}
