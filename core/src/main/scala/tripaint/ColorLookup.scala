package tripaint

import tripaint.coords.GlobalPixCoords

trait ColorLookup {
  def lookup(coords: GlobalPixCoords): Option[Color]
}
