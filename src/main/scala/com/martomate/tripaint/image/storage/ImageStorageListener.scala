package com.martomate.tripaint.image.storage

import com.martomate.tripaint.image.coords.TriangleCoords
import scalafx.scene.paint.Color

trait ImageStorageListener {
  def onPixelChanged(coords: TriangleCoords, from: Color, to: Color): Unit
}
