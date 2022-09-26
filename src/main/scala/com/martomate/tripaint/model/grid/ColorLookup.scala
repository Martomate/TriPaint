package com.martomate.tripaint.model.grid

import com.martomate.tripaint.model.Color
import com.martomate.tripaint.model.coords.GlobalPixCoords

trait ColorLookup {
  def lookup(coords: GlobalPixCoords): Option[Color]
}
