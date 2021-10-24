package com.martomate.tripaint.model.image.storage

import com.martomate.tripaint.model.Color
import com.martomate.tripaint.model.coords.TriangleCoords
import com.martomate.tripaint.model.image.RegularImage
import com.martomate.tripaint.model.image.format.StorageFormat
import com.martomate.tripaint.util.Listenable

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

  def toRegularImage(format: StorageFormat): RegularImage = {
    val image = RegularImage.ofSize(imageSize, imageSize)
    for (y <- 0 until imageSize) {
      for (x <- 0 until 2 * y + 1) {
        val tCoords = TriangleCoords(x, y)
        val sCoords = format.transformToStorage(tCoords)

        image.setColor(sCoords.x, sCoords.y, get(tCoords))
      }
    }
    image
  }
}

