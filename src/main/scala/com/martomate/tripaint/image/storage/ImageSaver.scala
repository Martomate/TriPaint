package com.martomate.tripaint.image.storage

import java.awt.image.BufferedImage
import java.io.{File, IOException}

import javax.imageio.ImageIO
import scalafx.scene.paint.Color

trait ImageSaver {
  def save(): Boolean
}

class FileImageSaver(source: ImageSource, file: File) extends ImageSaver {
  override def save(): Boolean = {
    try {
      if (!file.exists()) {
        file.getParentFile.mkdirs()
        file.createNewFile()
      }
      val width = source.width
      val height = source.height

      val image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

      for (y <- 0 until height) {
        for (x <- 0 until width) {
          image.setRGB(x, y, colorToInt(source(x, y)))
        }
      }

      ImageIO.write(image, file.getName.substring(file.getName.lastIndexOf('.') + 1), file)
    } catch {
      case e: IOException =>
        e.printStackTrace()
        false
    }
  }

  private def colorToInt(col: Color): Int = {
    (col.opacity * 255).toInt << 24 |
    (col.red     * 255).toInt << 16 |
    (col.green   * 255).toInt <<  8 |
    (col.blue    * 255).toInt
  }
}
