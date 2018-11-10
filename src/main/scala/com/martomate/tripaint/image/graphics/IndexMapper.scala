package com.martomate.tripaint.image.graphics

import com.martomate.tripaint.image.coords.TriangleCoords

trait IndexMapper {
  def coordsAt(x: Double, y: Double): TriangleCoords
}
