package com.martomate.tripaint.model.image.storage

import com.martomate.tripaint.model.coords.TriangleCoords
import scalafx.scene.paint.Color

trait ImageStorageListener {
  def onPixelChanged(coords: TriangleCoords, from: Color, to: Color): Unit
}
