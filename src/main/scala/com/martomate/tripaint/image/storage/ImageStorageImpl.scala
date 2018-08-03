package com.martomate.tripaint.image.storage

import com.martomate.tripaint.image.SaveLocation
import com.martomate.tripaint.image.coords.{StorageCoords, TriangleCoords}
import com.martomate.tripaint.image.format.SimpleStorageFormat
import javax.imageio.ImageIO
import scalafx.scene.paint.Color

import scala.util.Try

class ImageStorageImpl private (val imageSize: Int, initialPixels: StorageCoords => Color) extends ImageStorage {
  private val coordsFormatter = new SimpleStorageFormat
  private val pixels = Array.tabulate(imageSize, imageSize)((x, y) => initialPixels(StorageCoords(x, y)))

  private def toStorage(coords: TriangleCoords): StorageCoords = coordsFormatter.transformToStorage(coords)
  private def fromStorage(coords: StorageCoords): TriangleCoords = coordsFormatter.transformFromStorage(coords)

  override protected def get(coords: TriangleCoords): Color = {
    val sc = toStorage(coords)
    pixels(sc.x)(sc.y)
  }

  override protected def set(coords: TriangleCoords, col: Color): Unit = {
    val sc = toStorage(coords)
    pixels(sc.x)(sc.y) = col
  }
}

object ImageStorageImpl extends ImageStorageFactory {
  def fromBGColor(bgColor: Color, imageSize: Int): ImageStorageImpl = {
    new ImageStorageImpl(imageSize, _ => bgColor)
  }

  def fromFile(saveInfo: SaveLocation, imageSize: Int): Try[ImageStorageImpl] = Try {
    val image = ImageIO.read(saveInfo.file)
    val (xOff, yOff) = saveInfo.offset
    val pixels = image.getRGB(xOff, yOff, imageSize, imageSize, null, 0, imageSize)
    new ImageStorageImpl(imageSize, coords => intToColor(pixels(coords.x + coords.y * imageSize)))
  }

  private def intToColor(value: Int): Color = Color.rgb(
    value >> 16 & 0xff,
    value >>  8 & 0xff,
    value >>  0 & 0xff,
   (value >> 24 & 0xff) / 255.0
  )
}
