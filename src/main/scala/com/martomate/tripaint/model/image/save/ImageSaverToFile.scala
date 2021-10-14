package com.martomate.tripaint.model.image.save

import com.martomate.tripaint.infrastructure.FileSystem
import com.martomate.tripaint.model.coords.TriangleCoords
import com.martomate.tripaint.model.image.SaveLocation
import com.martomate.tripaint.model.image.format.StorageFormat
import com.martomate.tripaint.model.image.storage.ImageStorage

import java.awt.image.BufferedImage

class ImageSaverToFile {

  def save(image: ImageStorage, format: StorageFormat, loc: SaveLocation, fileSystem: FileSystem): Boolean = {
    val oldImage = fileSystem.readImage(loc.file)
    val newImage = save(image, format, loc, oldImage)
    fileSystem.writeImage(newImage, loc.file)
  }

  def save(image: ImageStorage, format: StorageFormat, saveInfo: SaveLocation, oldImage: Option[BufferedImage]): BufferedImage = {
    val bufImage: BufferedImage = oldImage match {
      case Some(im) =>
        resizeImageIfNeeded(im, saveInfo.offset, image.imageSize)
      case None =>
        makeNewImage(image.imageSize + saveInfo.offset._1, image.imageSize + saveInfo.offset._2)
    }

    writeIntoImage(bufImage, image, saveInfo.offset, format)
    bufImage
  }

  private def copyImage(from: BufferedImage, to: BufferedImage): Unit = {
    val fromPixels = from.getRGB(0, 0, from.getWidth, from.getHeight, null, 0, from.getWidth)
    to.setRGB(0, 0, from.getWidth, from.getHeight, fromPixels, 0, from.getWidth)
  }

  private def resizeImageIfNeeded(image: BufferedImage, offset: (Int, Int), imageSize: Int): BufferedImage = {
    val neededWith = offset._1 + imageSize
    val neededHeight = offset._2 + imageSize

    if (image.getWidth < neededWith || image.getHeight < neededHeight) {
      val newImage = makeNewImage(neededWith, neededHeight)

      copyImage(image, newImage)
      newImage
    } else image
  }

  private def makeNewImage(width: Int, height: Int): BufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

  private def writeIntoImage(dest: BufferedImage, source: ImageStorage, offset: (Int, Int), format: StorageFormat): Unit = {
    for (y <- 0 until source.imageSize) {
      for (x <- 0 until 2 * y + 1) {
        val tCoords = TriangleCoords(x, y)
        val sCoords = format.transformToStorage(tCoords)

        dest.setRGB(sCoords.x + offset._1, sCoords.y + offset._2, source(tCoords).toInt)
      }
    }
  }

}
