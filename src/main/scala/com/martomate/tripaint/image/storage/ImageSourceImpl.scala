package com.martomate.tripaint.image.storage

import java.io.File

import javax.imageio.ImageIO
import scalafx.scene.paint.Color

import scala.collection.mutable
import scala.util.{Success, Try}

class ImageSourceImpl(val width: Int, val height: Int) extends ImageSource {
  protected val pixels: Array[Color] = new Array(width * height)

  override def apply(x: Int, y: Int): Color = pixels(x + y * width)

  override def update(x: Int, y: Int, col: Color): Unit = if (col != apply(x, y)) {
    pixels(x + y * width) = col
    if (!hasChanged()) {
      hasChanged() = true
      notifyListeners(_.onImageSourceSaved(false))
    }
    notifyListeners(_.onPixelChanged(x, y))
  }
}

class UnboundImageSource(imageSize: Int, initialColor: Color) extends ImageSourceImpl(imageSize, imageSize) {
  pixels.transform(_ => initialColor)
}

object ImageSourceImpl {
  private val sources: mutable.Map[File, ImageSource] = mutable.Map.empty

  def fromFile(file: File): Try[ImageSource] = {
    if (sources contains file) Success(sources(file))
    else Try {
      val image = ImageIO.read(file)
      val w = image.getWidth
      val h = image.getHeight
      val pix = image.getRGB(0, 0, w, h, null, 0, w)
      val source = new ImageSourceImpl(w, h)

      for (i <- 0 until pix.length) {
        source.pixels(i) = Color.rgb(
          pix(i) >> 16 & 0xff,
          pix(i) >> 8 & 0xff,
          pix(i) >> 0 & 0xff,
          (pix(i) >> 24 & 0xff) / 255.0)
      }

      source.hasChanged() = false
      source.imageSaver = new FileImageSaver(source, file)
      sources(file) = source
      source
    }
  }
}
