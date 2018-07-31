package com.martomate.tripaint.image2.storage

import com.martomate.tripaint.image2.coords.TriangleCoords
import com.martomate.tripaint.image2.save.ImageSaveInfo
import javax.imageio.ImageIO
import scalafx.scene.paint.Color

class ImageStorageImpl private (val imageSize: Int, initialPixels: (Int, Int) => Color, startAsChanged: Boolean) extends ImageStorage {
  private val pixels = Array.tabulate(imageSize, imageSize)(initialPixels)

  _changed.value = startAsChanged

  override protected def get(coords: TriangleCoords): Color = pixels(coords.x)(coords.y)

  override protected def set(coords: TriangleCoords, col: Color): Unit = pixels(coords.x)(coords.y) = col
}

object ImageStorageImpl {
  def fromBGColor(bgColor: Color, imageSize: Int): ImageStorageImpl = {
    new ImageStorageImpl(imageSize, (_, _) => bgColor, true)
  }

  def fromFile(saveInfo: ImageSaveInfo, imageSize: Int): ImageStorageImpl = {
    val image = ImageIO.read(saveInfo.saveLocation.file)
    val offset = saveInfo.saveLocation.offset.getOrElse(0, 0)
    val pixels = image.getRGB(offset._1, offset._2, imageSize, imageSize, null, 0, imageSize)
    new ImageStorageImpl(imageSize, (x, y) => new Color(intToColor(pixels(x + y * imageSize))), false)
  }

  private def intToColor(value: Int): Color = Color.rgb(
    value >> 16 & 0xff,
    value >>  8 & 0xff,
    value >>  0 & 0xff,
   (value >> 24 & 0xff) / 255.0
  )
}