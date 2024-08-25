package tripaint

import tripaint.color.Color
import tripaint.coords.GlobalPixCoords

trait ColorLookup {
  def lookup(coords: GlobalPixCoords): Option[Color]
}
