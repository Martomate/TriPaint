package com.martomate.tripaint.image

import java.awt.image.BufferedImage
import java.io.File

import com.martomate.tripaint.image.effects.Effect
import com.martomate.tripaint.{EditMode, ImagePane}
import com.martomate.tripaint.undo.UndoManager
import javafx.scene.control.Tooltip
import javafx.scene.input.{MouseButton, MouseEvent, ScrollEvent}
import scalafx.beans.property._
import scalafx.scene.canvas.Canvas
import scalafx.scene.layout.Pane
import scalafx.scene.paint.Color

object TriImage {
  val previewSize = 64

  def loadFromFile(coords: TriImageCoords, file: File, imagePane: ImagePane, offset: Option[(Int, Int)], imageSize: Int): TriImage = {
    new TriImage(coords, ImageStorage.loadFromFile(file, offset, imageSize), imagePane)
  }

  def loadFromFile(coords: TriImageCoords, file: File, imagePane: ImagePane): TriImage = {
    new TriImage(coords, ImageStorage.loadFromFile(file), imagePane)
  }

  def apply(coords: TriImageCoords, imageSize: Int, imagePane: ImagePane) =
    new TriImage(coords, new ImageStorage(imageSize, new Color(imagePane.secondaryColor())), imagePane)
}

case class TriImageCoords(x: Int, y: Int) {
  val vertices: Seq[(Double, Double)] = {
    val pts = if (x % 2 == 0) Seq(
      (x / 2    , y),
      (x / 2 + 1, y),
      (x / 2    , y + 1)
    ) else Seq(
      (x / 2 + 1, y + 1),
      (x / 2    , y + 1),
      (x / 2 + 1, y)
    )
    pts map {
      case (xx, yy) => (xx + yy * 0.5, -yy * Math.sqrt(3) / 2)
    }
  }

  val centroid: (Double, Double) = {
    val sum: (Double, Double) = vertices.fold((0d, 0d))((t1, t2) => (t1._1 + t2._1, t1._2 + t2._2))
    (sum._1 / 3, sum._2 / 3)
  }

  def xOff: Double = centroid._1
  def yOff: Double = centroid._2
}

class TriImageCanvas(init_width: Double) extends Canvas(init_width, init_width * Math.sqrt(3) / 2) {
  object points {
    val x: Array[Double] = new Array(3)
    val y: Array[Double] = new Array(3)
  }

  def clearCanvas(): Unit = graphicsContext2D.clearRect(0, 0, width(), height())

  def storeCoords(index: Int, xx: Double, yy: Double): Unit = {
    points.x(index) = xx * width()
    points.y(index) = yy * height()
  }

  def drawTriangle(color: Color, strokeInstead: Boolean): Unit = {
    val gc = graphicsContext2D
    if (strokeInstead) {
      gc.setStroke(color)
      gc.strokePolygon(points.x, points.y, 3)
    } else {
      gc.setFill(color)
      gc.fillPolygon(points.x, points.y, 3)
    }
  }
}

class TriImage private(val coords: TriImageCoords, val storage: ImageStorage, val imagePane: ImagePane) extends Pane {
  private def panX = imagePane.xScroll + coords.xOff * imagePane.sideLength
  private def panY = imagePane.yScroll + coords.yOff * imagePane.sideLength
  private def zoom = imagePane.globalZoom

  val canvas: TriImageCanvas = new TriImageCanvas(imagePane.imageSize)
  val preview: TriImageCanvas = new TriImageCanvas(TriImage.previewSize)

  val selected = new BooleanProperty()
  def isSelected = selected()

  this.children add canvas
  if (coords.x % 2 == 1) canvas.rotate() += 180
  updateCanvasSize()

  private object indexMap {
    val xInt:  Array[Int] = new Array(3)
    val yInt:  Array[Int] = new Array(3)
    val image: BufferedImage = new BufferedImage(
      Math.ceil(canvas.width()  / zoom * 3).toInt,
      Math.ceil(canvas.height() / zoom * 3).toInt,
      BufferedImage.TYPE_INT_RGB
    )

    def indexAt(x: Double, y: Double): Int = {
      val xx = (x / canvas.width()  * image.getWidth ).toInt
      val yy = (y / canvas.height() * image.getHeight).toInt

      if (xx >= 0 && xx < image.getWidth() && yy >= 0 && yy < image.getHeight())
        (image.getRGB(xx, yy) & 0xffffff) - 1
      else -1
    }

    def performIndexMapping(index: Int): Unit = {
      val indexGraphics = image.getGraphics
      val indexColor = new java.awt.Color(index + 1)
      indexGraphics.setColor(indexColor)
      indexGraphics.drawPolygon(xInt, yInt, 3)
      indexGraphics.fillPolygon(xInt, yInt, 3)
    }

    def storeCoords(index: Int, xx: Double, yy: Double): Unit = {
      xInt(index) = Math.round(xx * image.getWidth).toInt
      yInt(index) = Math.round(yy * image.getHeight).toInt
    }
  }

  redraw(true)

  private val undoManager = new UndoManager
  def undo: Boolean = undoManager.undo
  def redo: Boolean = undoManager.redo

  private def fill(index: Int, color: Color): Unit = {
    if (index != -1) {
      val referenceColor = storage(index)
      val places = storage.searchWithIndex(index, (_, col) => col == referenceColor)
      places.foreach(p => drawAtCoords(p, color))
    }
  }

  onMouseReleased = e => {
    if (!e.isConsumed) {
      if (isSelected) updateAfterDraw()
    }
  }

  onScroll = e => {
    if (e.isControlDown) {
      updateCanvasSize()

      redraw(true)
    } else {

      updateLocation()
    }
  }

  private def drawAt(x: Double, y: Double, color: Color): Unit = {
    drawAt(indexAt(x, y), color)
  }

  def drawAt(index: Int, color: Color): Unit = {
    if (index >= 0 && index < storage.numPixels) {
      storage(index) = color

      val coords = storage.coordsFromIndex(index)
      drawTriangle(coords, doIndexMapping = false)
    }
  }

  def drawAtCoords(coords: Coord, color: Color): Unit = {
    val index = coords.index
    if (index >= 0 && index < storage.numPixels) {
      storage(index) = color

      drawTriangle(coords, doIndexMapping = false)
    }
  }

  def pointToCoord(index: Int): Coord = Coord.fromIndex(index, storage.imageSize)

  def indexAt(x: Double, y: Double): Int = indexMap.indexAt(x, y)

  private[image] def drawTriangle(coords: Coord, doIndexMapping: Boolean, strokeInstead: Boolean = false): Unit = {
    val Coord(x, y, index) = coords
    val yp = y
    val xp = x * 0.5 - (yp - storage.imageSize + 1) * 0.5

    storeAllCoords(xp, yp, x % 2 == 1)

    canvas.drawTriangle(storage(index), strokeInstead)
    preview.drawTriangle(storage(index), strokeInstead)

    if (doIndexMapping) indexMap.performIndexMapping(index)
  }

  def redraw(doIndexMapping: Boolean): Unit = {
    canvas.clearCanvas()
    preview.clearCanvas()

    for (y <- 0 until storage.imageSize) {
      for (x <- 0 until y * 2 + 1) {
        drawTriangle(Coord.fromXY(x, y, storage.imageSize), doIndexMapping, strokeInstead = true)
      }
    }

    for (y <- 0 until storage.imageSize) {
      for (x <- 0 until y * 2 + 1) {
        drawTriangle(Coord.fromXY(x, y, storage.imageSize), doIndexMapping)
      }
    }
  }

  private def storeAllCoords(xp: Double, yp: Double, upsideDown: Boolean): Unit = {
    if (upsideDown) {
      storeCoords(0, xp,       yp)
      storeCoords(1, xp + 1.0, yp)
      storeCoords(2, xp + 0.5, yp + 1.0)
    } else {
      storeCoords(0, xp,       yp + 1.0)
      storeCoords(1, xp + 1.0, yp + 1.0)
      storeCoords(2, xp + 0.5, yp)
    }
  }

  private def storeCoords(index: Int, xPos: Double, yPos: Double): Unit = {
    val xx = xPos / storage.imageSize
    val yy = yPos / storage.imageSize

    canvas.storeCoords(index, xx, yy)
    preview.storeCoords(index, xx, yy)
    indexMap.storeCoords(index, xx, yy)
  }

  private def updateCanvasSize(): Unit = {
    canvas.width = (storage.imageSize * 2 + 1) * zoom
    canvas.height = canvas.width() * Math.sqrt(3) / 2

    updateLocation()
  }

  def move(dx: Double, dy: Double): Unit = {
    ???

    updateLocation()
  }

  def scale(amt: Double): Unit = {
    ???

    redraw(true)
  }

  def rotate(angle: Double): Unit = {
    canvas.rotate() += angle
  }

  private def updateAfterDraw(): Unit = {
    undoManager.append(storage.cumulativeChange.done(EditMode.currentMode.tooltipText, this))
    redraw(false)

  }

  def applyEffect(effect: Effect): Unit = {
    effect.action(storage)

    updateAfterDraw()
  }

  def updateLocation(): Unit = {
    // adjustment caused by canvas center not being the wanted rotation center (i.e. the centroid)
    val adjLen = canvas.height() / 6
    val angle = canvas.rotate() / 180 * math.Pi
    val (dx, dy) = (-adjLen * math.sin(angle), -adjLen * math.cos(angle))
    canvas.relocate((imagePane.width() - canvas.width()) / 2 + panX + dx, (imagePane.height() - canvas.height()) / 2 + panY + dy)
  }

  def save: Boolean = storage.save

  def setSaveLocation(location: SaveLocation): Unit = {
    storage.saveLocation = location
  }

  def hasChanged: Boolean = storage.hasChanged

  def hasChangedProperty: ReadOnlyBooleanProperty = storage.hasChangedProperty

  private val _toolTip = new ReadOnlyObjectWrapper[Tooltip](null, null, new Tooltip("Not saved"))

  def toolTip: ReadOnlyObjectProperty[Tooltip] = _toolTip.readOnlyProperty

  _toolTip().textProperty().bind(storage.infoText)
}
