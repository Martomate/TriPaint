package com.martomate.tripaint.image.save

import java.awt.image.BufferedImage
import java.io.File

import com.martomate.tripaint.image.SaveLocation
import com.martomate.tripaint.image.coords.TriangleCoords
import com.martomate.tripaint.image.format.StorageFormat
import com.martomate.tripaint.image.storage.ImageStorage
import javax.imageio.ImageIO
import scalafx.scene.paint.Color

import scala.util.Try

class ImageSaverToFile(format: StorageFormat) extends ImageSaver {

  def save(image: ImageStorage, saveInfo: SaveLocation): Boolean = {
    val SaveLocation(file, offset) = saveInfo

    val oldImage: Option[BufferedImage] = readImageAt(file)

    val bufImage: BufferedImage = oldImage
      .map(im => resizeImageIfNeeded(im, offset, image.imageSize))
      .getOrElse(makeNewImage(image.imageSize, image.imageSize))

    writeImage(bufImage, image, format)
    writeImageToFile(bufImage, file)
  }

  private def readImageAt(file: File): Option[BufferedImage] = Try(ImageIO.read(file)).toOption

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

  private def colorToInt(col: Color): Int = {
    (col.opacity * 255).toInt << 24 |
    (col.red     * 255).toInt << 16 |
    (col.green   * 255).toInt <<  8 |
    (col.blue    * 255).toInt
  }

  private def writeImage(dest: BufferedImage, source: ImageStorage, format: StorageFormat): Unit = {
    for (y <- 0 until source.imageSize) {
      for (x <- 0 until 2 * y + 1) {
        val tCoords = TriangleCoords(x, y)
        val sCoords = format.transformToStorage(tCoords)

        dest.setRGB(sCoords.x, sCoords.y, colorToInt(source(tCoords)))
      }
    }
  }

  private def getExtension(file: File): String = file.getName.substring(file.getName.lastIndexOf('.') + 1)

  private def writeImageToFile(image: BufferedImage, file: File): Boolean = ImageIO.write(image, getExtension(file).toUpperCase, file)
}
