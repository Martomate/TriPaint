package com.martomate.tripaint.model.effects

import com.martomate.tripaint.model.Color
import com.martomate.tripaint.model.coords.GlobalPixCoords
import com.martomate.tripaint.model.grid.ColorLookup

class BlurEffect(radius: Int) extends LocalEffect {
  def name: String = "Blur"

  private val radiusSq = radius * radius

  override protected def predicate(image: ColorLookup, here: GlobalPixCoords)(coords: GlobalPixCoords, color: Color): Boolean = {
    coords.distanceSq(here) <= radiusSq * 1.5
  }

  override protected def weightedColor(image: ColorLookup, here: GlobalPixCoords)(coords: GlobalPixCoords): (Double, Color) = {
    (math.exp(-2 * coords.distanceSq(here) / radiusSq), image.lookup(coords).get)
  }
}
