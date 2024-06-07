package tripaint.model.effects

import tripaint.model.{Color, ColorLookup}
import tripaint.model.coords.GlobalPixCoords

class MotionBlurEffect(radius: Int) extends LocalEffect {
  def name: String = "Motion blur"

  private val radiusSq = radius.toDouble * radius

  override protected def predicate(
      image: ColorLookup,
      here: GlobalPixCoords
  )(coords: GlobalPixCoords, color: Color): Boolean = {
    here.y == coords.y && math.pow(here.x - coords.x, 2) <= radiusSq * 1.5
  }

  override protected def weightedColor(image: ColorLookup, here: GlobalPixCoords)(
      coords: GlobalPixCoords
  ): (Double, Color) = {
    (math.exp(-2 * math.pow(here.x - coords.x, 2) / radiusSq), image.lookup(coords).get)
  }
}
