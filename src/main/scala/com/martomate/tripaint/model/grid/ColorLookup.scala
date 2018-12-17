package com.martomate.tripaint.model.grid

import com.martomate.tripaint.model.coords.GlobalPixCoords
import scalafx.scene.paint.Color

trait ColorLookup {
  def lookup(coords: GlobalPixCoords): Option[Color]
}
