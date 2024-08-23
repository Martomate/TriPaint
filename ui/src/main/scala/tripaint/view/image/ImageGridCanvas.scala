package tripaint.view.image

import tripaint.coords.{GlobalPixCoords, GridCoords, PixelCoords}
import tripaint.grid.{GridCell, ImageGrid}

import javafx.scene.canvas.Canvas
import javafx.scene.image.{PixelFormat, WritableImage}

class ImageGridCanvas(images: ImageGrid) extends Canvas {
  private var scale: Double = 1
  private var displacement: (Double, Double) = (0, 0)
  private var width: Double = 0
  private var height: Double = 0

  private inline val sc = 2 // pixel scale used to ensure shard images on high dpi displays

  this.widthProperty.addListener(_ => this.width = getWidth)
  this.heightProperty.addListener(_ => this.height = getHeight)

  def setScale(scale: Double): Unit = {
    this.scale = scale
  }

  def setDisplacement(dx: Double, dy: Double): Unit = {
    this.displacement = (dx, dy)
  }

  def coordsAt(x: Double, y: Double): GlobalPixCoords = {
    val (xScroll, yScroll) = this.displacement

    val yy = -(y - yScroll - this.height / 2) / this.scale / Math.sqrt(3)
    val xx = ((x - xScroll - this.width / 2) / this.scale - yy) / 2

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

    val y = -yy * Math.sqrt(3) * this.scale + this.height / 2 + yScroll
    val x = (xx * 2 + yy) * this.scale + this.width / 2 + xScroll

    (x, y)
  }

  def redraw(): Unit = {
    val w = this.width.toInt
    val h = this.height.toInt

    redraw(0, 0, w, h)
  }

  def redraw(startX: Int, startY: Int, w: Int, h: Int): Unit = {
    if images.imageSize < 0 then {
      // This "hack" is needed right at the beginning before the user has specified the size
      return
    }

    val buffer = this.renderRegion(startX, startY, w, h)

    getGraphicsContext2D.clearRect(startX + 1, startY + 1, w - 2, h - 2)
    getGraphicsContext2D.drawImage(buffer, 0, 0, w * sc, h * sc, startX, startY, w, h)
  }

  private def calculateCoordsForRegion(startX: Int, startY: Int, w: Int, h: Int): Array[Long] = {
    val imageSize = this.images.imageSize
    val scInv = 1.0 / sc

    val ww = w * sc
    val hh = h * sc
    val buffer = new Array[Long](ww * hh)

    val y0 = startY * sc
    val x0 = startX * sc
    val y1 = (startY + h) * sc
    val x1 = (startX + w) * sc

    val (xScroll, yScroll) = this.displacement
    val canvasHeight = this.height
    val canvasWidth = this.width
    val canvasInvScale = 1.0 / this.scale
    val invSqrt3 = 1.0 / Math.sqrt(3)

    val canvasStartY = yScroll + canvasHeight * 0.5
    val canvasStartX = xScroll + canvasWidth * 0.5

    var yi = y0
    while yi < y1 do {
      var xi = x0
      while xi < x1 do {
        val x = xi * scInv
        val y = yi * scInv

        val yy = -(y - canvasStartY) * canvasInvScale * invSqrt3
        val xx = ((x - canvasStartX) * canvasInvScale - yy) * 0.5

        val xInt = Math.floor(xx).toInt
        val yInt = Math.floor(yy).toInt

        var cx = xInt * 2
        val cy = yInt
        if xx - xInt.toDouble > 1 - (yy - yInt.toDouble) then {
          cx += 1
        }

        val c = PixelCoords.fromGlobalCoords(cx, cy, imageSize).value

        buffer(xi - x0 + (yi - y0) * ww) = c

        xi += 1
      }
      yi += 1
    }

    buffer
  }

  private def renderRegion(startX: Int, startY: Int, w: Int, h: Int): WritableImage = {
    val scInv = 1.0 / sc
    val imageSize = images.imageSize

    val ww = w * sc
    val hh = h * sc
    val buffer = new Array[Int](ww * hh) // starts with transparent pixels (value 0)

    var prevCellCoords: GridCoords = GridCoords(0x7fff, 0x7fff) // we assume this cell is never used
    var prevCell: GridCell = null

    val y0 = startY * sc
    val x0 = startX * sc
    val y1 = (startY + h) * sc
    val x1 = (startX + w) * sc

    inline val stepSize = 64

    val coordsBuffer = this.calculateCoordsForRegion(startX, startY, w, h)

    var ys = y0
    while ys < y1 do {
      val ySteps = Math.min(y1 - ys, stepSize)

      var xs = x0
      while xs < x1 do {
        val xSteps = Math.min(x1 - xs, stepSize)

        var dy = 0
        while dy < ySteps do {
          val y = ys + dy

          var dx = 0
          while dx < xSteps do {
            val x = xs + dx

            val coords = new PixelCoords(coordsBuffer(x - x0 + (y - y0) * ww))
            val cell = if coords.image == prevCellCoords then {
              prevCell
            } else {
              prevCellCoords = coords.image
              val cell = images(coords.image)
              prevCell = cell
              cell
            }
            if cell != null then {
              buffer(x - x0 + (y - y0) * ww) = cell.storage.getColorArgb(coords.pix)
            }

            dx += 1
          }
          dy += 1
        }
        xs += stepSize
      }
      ys += stepSize
    }

    val image = WritableImage(w * sc, h * sc)
    val f = PixelFormat.getIntArgbInstance
    image.getPixelWriter.setPixels(0, 0, w * sc, h * sc, f, buffer, 0, w * sc)

    image
  }
}
