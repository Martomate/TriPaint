package com.martomate.tripaint.model.image.storage

import com.martomate.tripaint.model.Color
import com.martomate.tripaint.model.coords.TriangleCoords

trait ImageStorageListener {
  def onPixelChanged(coords: TriangleCoords, from: Color, to: Color): Unit
}
