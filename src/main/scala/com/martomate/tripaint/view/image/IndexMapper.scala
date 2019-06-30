package com.martomate.tripaint.view.image

import com.martomate.tripaint.model.coords.TriangleCoords

trait IndexMapper {
  def coordsAt(x: Double, y: Double): TriangleCoords
}
