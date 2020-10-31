package com.martomate.tripaint.model.image.storage

import com.martomate.tripaint.model.coords.TriangleCoords
import com.martomate.tripaint.util.Listenable
import scalafx.scene.paint.Color

trait ImageStorage extends Listenable[ImageStorageListener] {
  val imageSize: Int

  protected def get(coords: TriangleCoords): Color
  protected def set(coords: TriangleCoords, col: Color): Unit

  final def contains(coords: TriangleCoords): Boolean = coords.y < imageSize

  final def apply(coords: TriangleCoords): Color = get(coords)
  final def update(coords: TriangleCoords, col: Color): Unit = {
    val before = apply(coords)
    if (before != col) {
      set(coords, col)
      notifyListeners(_.onPixelChanged(coords, before, col))
    }
  }

  def allPixels: IndexedSeq[TriangleCoords] = for {
    y <- 0 until imageSize
    x <- 0 until 2 * y + 1
  } yield TriangleCoords(x, y)
}

