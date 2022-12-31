package com.martomate.tripaint.view.image

import com.martomate.tripaint.model.Color
import com.martomate.tripaint.model.coords.TriangleCoords
import com.martomate.tripaint.model.image.storage.ImageStorage
import javafx.scene.image.PixelFormat
import scalafx.scene.canvas.Canvas

class TriImageCanvas(init_width: Double, imageSize: Int)
    extends Canvas(init_width, init_width * Math.sqrt(3) / 2) {
  private val coordsToRealConverter = new TriangleCoordsToReal[Double](
    imageSize,
    new Array(_),
    (xx, yy) => (xx * width(), yy * height())
  )

  def setCanvasSize(width: Double): Unit =
    this.width = width
    this.height = width * Math.sqrt(3) / 2

  def setCanvasLocationUsingCenter(centerX: Double, centerY: Double): Unit =
    // adjustment caused by canvas center not being the wanted rotation center (i.e. the centroid)
    val adjLen = this.height() / 6
    val angle = this.rotate() / 180 * math.Pi
    val dx = -adjLen * math.sin(angle)
    val dy = -adjLen * math.cos(angle)
    this.relocate(centerX - this.width() / 2 + dx, centerY - this.height() / 2 + dy)

  def clearCanvas(): Unit = graphicsContext2D.clearRect(-1, -1, width() + 1, height() + 1)

  def drawTriangle(coords: TriangleCoords, color: Color, pixels: ImageStorage): Unit = {
    val gc = graphicsContext2D
    val indexMap = new IndexMap(imageSize)

    val (xLo, yLo, xHi, yHi) = triangleBoundingRect(coords)

    for (y <- yLo.toInt - 1 to yHi.toInt + 1) {
      for (x <- xLo.toInt - 1 to xHi.toInt + 1) {
        val c = indexMap.coordsAt(x / width(), y / height())
        if (c != null) {
          if (c == coords)
            gc.pixelWriter.setColor(x, y, color.toFXColor)
          else
            gc.pixelWriter.setColor(x, y, pixels(c).toFXColor)
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
              image(dx + dy * 16) = c.withAlpha(1).toInt
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
    var xHi, yHi = 0.0

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
