package com.martomate.tripaint.model.image.save

import com.martomate.tripaint.model.coords.TriangleCoords
import com.martomate.tripaint.model.image.SaveLocation
import com.martomate.tripaint.model.image.format.StorageFormat
import com.martomate.tripaint.model.image.storage.ImageStorage
import scalafx.scene.paint.Color

import java.awt.image.BufferedImage

class ImageSaverToFile {

  def save(image: ImageStorage, format: StorageFormat, saveInfo: SaveLocation, oldImage: Option[BufferedImage]): BufferedImage = {
    val bufImage: BufferedImage = createDestinationImage(image, saveInfo.offset, oldImage)

    writeImage(bufImage, image, saveInfo.offset, format)
    bufImage
  }

  private def createDestinationImage(image: ImageStorage, offset: (Int, Int), oldImage: Option[BufferedImage]) = {
    oldImage
      .map(im => resizeImageIfNeeded(im, offset, image.imageSize))
      .getOrElse(makeNewImage(image.imageSize + offset._1, image.imageSize + offset._2))
  }

  private def copyImage(from: BufferedImage, to: BufferedImage): Unit = {
    val fromPixels = from.getRGB(0, 0, from.getWidth, from.getHeight, null, 0, from.getWidth)
    to.setRGB(0, 0, from.getWidth, from.getHeight, fromPixels, 0, from.getWidth)
  }

  private def resizeImageIfNeeded(image: BufferedImage, offset: (Int, Int), imageSize: Int): BufferedImage = {
    val sizeNeeded = (offset._1 + imageSize, offset._2 + imageSize)
    val imSize = (image.getWidth, image.getHeight)

    if (imSize._1 < sizeNeeded._1 || imSize._2 < sizeNeeded._2) {
      val newImage = makeNewImage(sizeNeeded._1, sizeNeeded._2)

      copyImage(image, newImage)
      newImage
    } else image
  }

  private def makeNewImage(width: Int, height: Int): BufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

  private def writeImage(dest: BufferedImage, source: ImageStorage, offset: (Int, Int), format: StorageFormat): Unit = {
    for (y <- 0 until source.imageSize) {
      for (x <- 0 until 2 * y + 1) {
        val tCoords = TriangleCoords(x, y)
        val sCoords = format.transformToStorage(tCoords)

        dest.setRGB(sCoords.x + offset._1, sCoords.y + offset._2, colorToInt(source(tCoords)))
      }
    }
  }

  protected def colorToInt(col: Color): Int = {
    (col.opacity * 255).toInt << 24 |
      (col.red     * 255).toInt << 16 |
      (col.green   * 255).toInt <<  8 |
      (col.blue    * 255).toInt
  }

}
