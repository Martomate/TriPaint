package tripaint.view.image

import tripaint.coords.TriangleCoords
import tripaint.image.ImageStorage

import javafx.scene.canvas.Canvas
import javafx.scene.image.{PixelFormat, WritableImage}
import tripaint.color.Color

class TriImageCanvas(init_width: Double, imageSize: Int)
    extends Canvas(init_width, init_width * Math.sqrt(3) / 2) {

  private inline val sc = 2

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
    val (xLo, yLo, xHi, yHi) = triangleBoundingRect(coords)

    redraw(pixels, xLo.toInt - 1, yLo.toInt - 1, xHi.toInt + 2, yHi.toInt + 2)
  }

  def redraw(pixels: ImageStorage): Unit = {
    redraw(pixels, 0, 0, getWidth.toInt, getHeight.toInt)
  }

  def redraw(pixels: ImageStorage, xLo: Int, yLo: Int, xHi: Int, yHi: Int): Unit =
    this.synchronized {
      val gc = getGraphicsContext2D
      val indexMap = new IndexMap(imageSize)

      val x0 = xLo * sc
      val y0 = yLo * sc
      val x1 = xHi * sc
      val y1 = yHi * sc

      val width = this.getWidth * sc
      val height = this.getHeight * sc

      val image = new Array[Int](16 * sc * 16 * sc)
      val f = PixelFormat.getIntArgbInstance

      gc.clearRect(xLo + 1, yLo + 1, xHi - xLo - 2, yHi - yLo - 2)

      for yc <- y0 until y1 by 16 * sc do {
        val h = Math.min(y1 - yc, 16 * sc)
        for xc <- x0 until x1 by 16 * sc do {
          val w = Math.min(x1 - xc, 16 * sc)

          for dy <- 0 until h do {
            val y = yc + dy
            for dx <- 0 until w do {
              val x = xc + dx

              val col = indexMap.coordsAt(x.toDouble / width, y.toDouble / height) match {
                case Some(coords) => pixels.getColor(coords).withAlpha(1).toInt
                case None         => 0
              }
              image(dx + dy * 16 * sc) = col
            }
          }
          val im = WritableImage(16 * sc, 16 * sc)
          im.getPixelWriter.setPixels(0, 0, w, h, f, image, 0, 16 * sc)
          gc.drawImage(im, 0, 0, w, h, xc / sc, yc / sc, w / sc, h / sc)
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
