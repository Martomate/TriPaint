package tripaint.view.image

import tripaint.Color
import tripaint.coords.{GlobalPixCoords, PixelCoords}
import tripaint.model.ImageGrid
import tripaint.model.image.RegularImage

import javafx.scene.image.PixelFormat
import scalafx.scene.canvas.Canvas

class ImageGridCanvas(images: ImageGrid) extends Canvas {
  private var scale: Double = 1
  private var displacement: (Double, Double) = (0, 0)

  def setScale(scale: Double): Unit = {
    this.scale = scale
  }

  def setDisplacement(dx: Double, dy: Double): Unit = {
    this.displacement = (dx, dy)
  }

  def coordsAt(x: Double, y: Double): GlobalPixCoords = {
    val (xScroll, yScroll) = this.displacement

    val yy = -(y - yScroll - height() / 2) / this.scale / Math.sqrt(3)
    val xx = ((x - xScroll - width() / 2) / this.scale - yy) / 2

    val xInt = Math.floor(xx).toInt
    val yInt = Math.floor(yy).toInt

    if xx - xInt.toDouble > 1 - (yy - yInt.toDouble) then {
      GlobalPixCoords(xInt * 2 + 1, yInt)
    } else {
      GlobalPixCoords(xInt * 2, yInt)
    }
  }

  /** locationOf is the inverse of the coordsAt function */
  def locationOf(coords: GlobalPixCoords): (Double, Double) = {
    val (xScroll, yScroll) = this.displacement

    val (xInt, yInt) = (Math.floorDiv(coords.x, 2), coords.y)
    val (xx, yy) = (xInt + 0.5, yInt + 0.5)

    val y = -yy * Math.sqrt(3) * this.scale + height() / 2 + yScroll
    val x = (xx * 2 + yy) * this.scale + width() / 2 + xScroll

    (x, y)
  }

  def redraw(): Unit = {
    val w = width().toInt
    val h = height().toInt

    redraw(0, 0, w, h)
  }

  def redraw(startX: Int, startY: Int, w: Int, h: Int): Unit = {
    if images.imageSize < 0 then {
      // This "hack" is needed right at the beginning before the user has specified the size
      return
    }

    val buffer = RegularImage.ofSize(w, h)

    for y <- startY until (startY + h) do {
      for x <- startX until (startX + w) do {
        val coords = PixelCoords(this.coordsAt(x, y), images.imageSize)
        val col = images(coords.image) match {
          case Some(image) => image.storage.getColor(coords.pix)
          case None        => Color(0, 0, 0, 0)
        }
        buffer.setColor(x - startX, y - startY, col)
      }
    }

    val f = PixelFormat.getIntArgbInstance
    graphicsContext2D.pixelWriter.setPixels(startX, startY, w, h, f, buffer.toIntArray, 0, w)
  }
}
