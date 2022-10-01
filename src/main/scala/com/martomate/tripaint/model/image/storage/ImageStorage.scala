package com.martomate.tripaint.model.image.storage

import com.martomate.tripaint.model.Color
import com.martomate.tripaint.model.coords.{StorageCoords, TriangleCoords}
import com.martomate.tripaint.model.image.RegularImage
import com.martomate.tripaint.model.image.format.{SimpleStorageFormat, StorageFormat}
import com.martomate.tripaint.util.Listenable

import scala.util.Try

class ImageStorage private (val imageSize: Int, initialPixels: TriangleCoords => Color)
    extends Listenable[ImageStorageListener] {
  private val coordsFormatter = new SimpleStorageFormat
  private val pixels = Array.tabulate(imageSize, imageSize)((x, y) =>
    initialPixels(coordsFormatter.transformFromStorage(StorageCoords(x, y)))
  )

  private def get(coords: TriangleCoords): Color = {
    val sc = coordsFormatter.transformToStorage(coords)
    pixels(sc.x)(sc.y)
  }

  private def set(coords: TriangleCoords, col: Color): Unit = {
    val sc = coordsFormatter.transformToStorage(coords)
    pixels(sc.x)(sc.y) = col
  }

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

  def mkString(mapper: Color => Any = c => c): String = {
    pixels.map(_.map(c => mapper(c)).mkString(", ")).mkString("\n")
  }
}

object ImageStorage {
  def fromBGColor(bgColor: Color, imageSize: Int): ImageStorage = {
    new ImageStorage(imageSize, _ => bgColor)
  }

  def fromRegularImage(
      image: RegularImage,
      offset: StorageCoords,
      format: StorageFormat,
      imageSize: Int
  ): Try[ImageStorage] = Try {
    def colorAt(coords: TriangleCoords): Color = {
      val stCoords = format.transformToStorage(coords)
      image.getColor(offset.x + stCoords.x, offset.y + stCoords.y)
    }

    new ImageStorage(imageSize, coords => colorAt(coords))
  }
}
