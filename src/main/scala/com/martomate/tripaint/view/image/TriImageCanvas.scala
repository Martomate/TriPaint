package com.martomate.tripaint.view.image

import com.martomate.tripaint.model.coords.TriangleCoords
import com.martomate.tripaint.model.image.storage.ImageStorage
import javafx.scene.image.PixelFormat
import scalafx.scene.canvas.Canvas
import scalafx.scene.paint.Color

class TriImageCanvas(init_width: Double, imageSize: Int) extends Canvas(init_width, init_width * Math.sqrt(3) / 2) {
  private val coordsToRealConverter = new TriangleCoordsToReal[Double](imageSize, new Array(_), (xx, yy) => (xx * width(), yy * height()))

  def clearCanvas(): Unit = graphicsContext2D.clearRect(-1, -1, width()+1, height()+1)

  def drawTriangle(coords: TriangleCoords, color: Color, pixels: ImageStorage): Unit = {
    val gc = graphicsContext2D
    val indexMap = new IndexMap(imageSize)

    val (xLo, yLo, xHi, yHi) = triangleBoundingRect(coords)

    for (y <- yLo.toInt - 1 to yHi.toInt + 1) {
      for (x <- xLo.toInt - 1 to xHi.toInt + 1) {
        val c = indexMap.coordsAt(x / width(), y / height())
        if (c != null) {
          if (c == coords)
            gc.pixelWriter.setColor(x, y, color)
          else
            gc.pixelWriter.setColor(x, y, pixels(c))
        }
      }
    }
  }

  def redraw(pixels: ImageStorage): Unit = {
    val gc = graphicsContext2D
    val indexMap = new IndexMap(imageSize)

    val heightInt = height().toInt
    val widthInt = width().toInt

    val image = new Array[Int](16 * 16)

    for (yc <- 0 until heightInt by 16) {
      val h = Math.min(heightInt - yc, 16)
      for (xc <- 0 until widthInt by 16) {
        val w = Math.min(widthInt - xc, 16)

        var dy = 0
        while (dy < h) {
          val y = yc + dy
          var dx = 0
          while (dx < w) {
            val x = xc + dx

            val coords = indexMap.coordsAt(x.toDouble / widthInt, y.toDouble / heightInt)
            if (coords != null) {
              val c = pixels(coords)
              image(dx + dy * 16) = 0xff << 24 | (c.getRed * 255).toInt << 16 | (c.getGreen * 255).toInt << 8 | (c.getBlue * 255).toInt
            } else {
              image(dx + dy * 16) = 0
            }
            dx += 1
          }
          dy += 1
        }

        gc.getPixelWriter.setPixels(xc, yc, w, h, PixelFormat.getIntArgbInstance, image, 0, 16)
      }
    }
  }

  private def triangleBoundingRect(coords: TriangleCoords): (Double, Double, Double, Double) = {
    val (px, py) = coordsToRealConverter.triangleCornerPoints(coords)

    var xLo, yLo = 1.0e9
    var xHi, yHi = 0.0;

    for (i <- 0 until 3) {
      val x = px(i)
      val y = py(i)

      if (x < xLo) xLo = x
      if (y < yLo) yLo = y
      if (x > xHi) xHi = x
      if (y > yHi) yHi = y
    }

    (xLo, yLo, xHi, yHi)
  }
}

