package tripaint.model

import tripaint.model.coords.GlobalPixCoords

trait ColorLookup {
  def lookup(coords: GlobalPixCoords): Option[Color]
}
