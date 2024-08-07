package tripaint.view.image

import tripaint.Color
import tripaint.ScalaFxExt.*
import tripaint.coords.TriangleCoords
import tripaint.image.ImageStorage

import javafx.scene.canvas.Canvas
import javafx.scene.image.PixelFormat

class TriImageCanvas(init_width: Double, imageSize: Int)
    extends Canvas(init_width, init_width * Math.sqrt(3) / 2) {

  private val coordsToRealConverter =
    new TriangleCoordsToReal(imageSize, (xx, yy) => (xx * getWidth, yy * getHeight))

  def setCanvasSize(width: Double): Unit = {
    this.setWidth(width)
    this.setHeight(width * Math.sqrt(3) / 2)
  }

  def setCanvasLocationUsingCenter(centerX: Double, centerY: Double): Unit = {
    // adjustment caused by canvas center not being the wanted rotation center (i.e. the centroid)
    val adjLen = this.getHeight / 6
    val angle = this.getRotate / 180 * math.Pi
    val dx = -adjLen * math.sin(angle)
    val dy = -adjLen * math.cos(angle)
    this.relocate(centerX - this.getWidth / 2 + dx, centerY - this.getHeight / 2 + dy)
  }

  def clearCanvas(): Unit = {
    getGraphicsContext2D.clearRect(-1, -1, getWidth + 1, getHeight + 1)
  }

  def drawTriangle(coords: TriangleCoords, color: Color, pixels: ImageStorage): Unit = {
    val gc = getGraphicsContext2D
    val indexMap = new IndexMap(imageSize)

    val (xLo, yLo, xHi, yHi) = triangleBoundingRect(coords)

    for y <- yLo.toInt - 1 to yHi.toInt + 1 do {
      for x <- xLo.toInt - 1 to xHi.toInt + 1 do {
        val c = indexMap.coordsAt(x / getWidth, y / getHeight)
        if c != null then {
          val col = if c == coords then color else pixels.getColor(c)
          gc.getPixelWriter.setColor(x, y, col.toFXColor)
        }
      }
    }
  }

  def redraw(pixels: ImageStorage): Unit = {
    val gc = getGraphicsContext2D
    val indexMap = new IndexMap(imageSize)

    val heightInt = getHeight.toInt
    val widthInt = getWidth.toInt

    val image = new Array[Int](16 * 16)

    for yc <- 0 until heightInt by 16 do {
      val h = Math.min(heightInt - yc, 16)
      for xc <- 0 until widthInt by 16 do {
        val w = Math.min(widthInt - xc, 16)

        for dy <- 0 until h do {
          val y = yc + dy
          for dx <- 0 until w do {
            val x = xc + dx

            val coords = indexMap.coordsAt(x.toDouble / widthInt, y.toDouble / heightInt)
            val col = if coords != null then pixels.getColor(coords).withAlpha(1).toInt else 0
            image(dx + dy * 16) = col
          }
        }

        gc.getPixelWriter.setPixels(xc, yc, w, h, PixelFormat.getIntArgbInstance, image, 0, 16)
      }
    }
  }

  private def triangleBoundingRect(coords: TriangleCoords): (Double, Double, Double, Double) = {
    val (px, py) = coordsToRealConverter.triangleCornerPoints(coords)

    var xLo, yLo = 1.0e9
    var xHi, yHi = 0.0

    for i <- 0 until 3 do {
      val x = px(i)
      val y = py(i)

      if x < xLo then xLo = x
      if y < yLo then yLo = y
      if x > xHi then xHi = x
      if y > yHi then yHi = y
    }

    (xLo, yLo, xHi, yHi)
  }
}
