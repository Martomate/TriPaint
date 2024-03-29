package tripaint.model.effects

import tripaint.model.{Color, ColorLookup}
import tripaint.model.coords.GlobalPixCoords

class CellEffect extends LocalEffect {
  def name: String = "Cell"

  override protected def predicate(
      image: ColorLookup,
      here: GlobalPixCoords
  )(coords: GlobalPixCoords, color: Color): Boolean = {
    coords.cell == here.cell
  }

  override protected def weightedColor(image: ColorLookup, here: GlobalPixCoords)(
      coords: GlobalPixCoords
  ): (Double, Color) = {
    (1, image.lookup(coords).get)
  }
}
