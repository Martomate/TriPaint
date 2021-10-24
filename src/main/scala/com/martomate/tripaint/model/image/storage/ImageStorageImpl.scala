package com.martomate.tripaint.model.image.storage

import com.martomate.tripaint.model.Color
import com.martomate.tripaint.model.coords.{StorageCoords, TriangleCoords}
import com.martomate.tripaint.model.image.RegularImage
import com.martomate.tripaint.model.image.format.{SimpleStorageFormat, StorageFormat}

import scala.util.Try

class ImageStorageImpl private (val imageSize: Int, initialPixels: TriangleCoords => Color) extends ImageStorage {
  private val coordsFormatter = new SimpleStorageFormat
  private val pixels = Array.tabulate(imageSize, imageSize)((x, y) => initialPixels(coordsFormatter.transformFromStorage(StorageCoords(x, y))))

  override protected def get(coords: TriangleCoords): Color = {
    val sc = coordsFormatter.transformToStorage(coords)
    pixels(sc.x)(sc.y)
  }

  override protected def set(coords: TriangleCoords, col: Color): Unit = {
    val sc = coordsFormatter.transformToStorage(coords)
    pixels(sc.x)(sc.y) = col
  }

  def mkString(mapper: Color => Any = c => c): String = {
    pixels.map(_.map(c => mapper(c)).mkString(", ")).mkString("\n")
  }
}

object ImageStorageImpl extends ImageStorageFactory {
  def fromBGColor(bgColor: Color, imageSize: Int): ImageStorageImpl = {
    new ImageStorageImpl(imageSize, _ => bgColor)
  }

  def fromRegularImage(image: RegularImage, offset: StorageCoords, format: StorageFormat, imageSize: Int): Try[ImageStorageImpl] = Try {
    def colorAt(coords: TriangleCoords): Color = {
      val stCoords = format.transformToStorage(coords)
      image.getColor(offset.x + stCoords.x, offset.y + stCoords.y)
    }

    new ImageStorageImpl(imageSize, coords => colorAt(coords))
  }

}
