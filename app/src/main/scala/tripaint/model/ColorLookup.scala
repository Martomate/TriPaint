package tripaint.model

import tripaint.Color
import tripaint.coords.GlobalPixCoords

trait ColorLookup {
  def lookup(coords: GlobalPixCoords): Option[Color]
}
