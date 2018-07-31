package com.martomate.tripaint.image

import java.awt.image.BufferedImage

import com.martomate.tripaint.image.effects.Effect
import com.martomate.tripaint.image.storage._
import com.martomate.tripaint.undo.UndoManager
import com.martomate.tripaint.{EditMode, ImagePane, Listenable}
import scalafx.beans.property._
import scalafx.scene.SnapshotParameters
import scalafx.scene.canvas.Canvas
import scalafx.scene.control.Tooltip
import scalafx.scene.image.Image
import scalafx.scene.layout.Pane
import scalafx.scene.paint.Color

import scala.util.Try

object TriImage {
  val previewSize = 64

  def loadFromSource(coords: TriImageCoords, source: ImageSource, imagePane: ImagePane, offset: Option[(Int, Int)], imageSize: Int): Try[TriImage] = {
    ImageStorage.fromSource(source, offset, imageSize) map { storage =>
      new TriImage(coords, storage, imagePane)
    }
  }

  def apply(coords: TriImageCoords, imageStorage: ImageStorage, imagePane: ImagePane) =
    new TriImage(coords, imageStorage, imagePane)
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

class TriImageActualCanvas(init_width: Double) extends TriImageCanvas(init_width) {
  def updateLocation(panX: Double, panY: Double): Unit = {
    // adjustment caused by canvas center not being the wanted rotation center (i.e. the centroid)
    val adjLen = height() / 6
    val angle = rotate() / 180 * math.Pi
    val (dx, dy) = (-adjLen * math.sin(angle), -adjLen * math.cos(angle))
    relocate(-width() / 2 + panX + dx, -height() / 2 + panY + dy)
  }

  def updateCanvasSize(imageSize: Int, zoom: Double): Unit = {
    width = (imageSize * 2 + 1) * zoom
    height = width() * Math.sqrt(3) / 2
  }
}

trait IndexMapper {
  def indexAt(x: Double, y: Double): Int
}

class IndexMap(canvas: Canvas, init_zoom: Double) extends IndexMapper {
  val xInt:  Array[Int] = new Array(3)
  val yInt:  Array[Int] = new Array(3)
  val image: BufferedImage = new BufferedImage(
    Math.ceil(canvas.width()  / init_zoom * 3).toInt,
    Math.ceil(canvas.height() / init_zoom * 3).toInt,
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

trait TriImageView {
  private[image] def canvas: TriImageCanvas
}

class TriImagePreview(width: Double, val image: TriImage) extends Pane with TriImageView {
  private[image] val canvas: TriImageCanvas = new TriImageCanvas(width)

  children add canvas
  image.addListener(this)
  image.redraw(false)

  def toImage(params: SnapshotParameters): Image = canvas.snapshot(params, null)
}

class TriImagePane extends TriImageView {
  private[image] def canvas: TriImageCanvas = ???
}

class TriImage private(val coords: TriImageCoords, val storage: ImageStorage, val imagePane: ImagePane)
  extends Pane
    with ImageStorageListener
    with Listenable[TriImageView] {

  private def panX = coords.xOff * imagePane.sideLength
  private def panY = coords.yOff * imagePane.sideLength
  private def zoom = imagePane.globalZoom

  val canvas: TriImageActualCanvas = new TriImageActualCanvas(imagePane.imageSize)

  val selected = new BooleanProperty()
  def isSelected = selected()

  storage.addListener(this)
  children add canvas
  if (coords.x % 2 == 1) canvas.rotate() += 180
  updateCanvasSize()

  private val indexMap = new IndexMap(canvas, zoom)

  redraw(true)

  private val undoManager = new UndoManager
  def undo: Boolean = undoManager.undo
  def redo: Boolean = undoManager.redo

  onMouseReleased = e => {
    if (!e.isConsumed) {
      //if (isSelected)
        updateAfterDraw()
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

  def drawAt(index: Int, color: Color): Unit = {
    if (index >= 0 && index < storage.numPixels) {
      storage(index) = color
    }
  }

  def drawAtCoords(coords: Coord, color: Color): Unit = {
    val index = coords.index
    if (index >= 0 && index < storage.numPixels) {
      storage(index) = color
    }
  }

  def pointToCoord(index: Int): Coord = Coord.fromIndex(index, storage.imageSize)

  def indexAt(x: Double, y: Double): Int = {
    val pt = canvas.sceneToLocal(x, y)
    indexMap.indexAt(pt.getX, pt.getY)
  }

  private[image] def drawTriangle(coords: Coord, doIndexMapping: Boolean, strokeInstead: Boolean = false): Unit = {
    val yp = coords.y
    val xp = coords.x * 0.5 - (yp - storage.imageSize + 1) * 0.5

    storeAllCoords(xp, yp, coords.x % 2 == 1)

    canvas.drawTriangle(storage(coords.index), strokeInstead)
    notifyListeners(_.canvas.drawTriangle(storage(coords.index), strokeInstead))

    if (doIndexMapping) indexMap.performIndexMapping(coords.index)
  }

  def redraw(doIndexMapping: Boolean): Unit = {
    canvas.clearCanvas()
    notifyListeners(_.canvas.clearCanvas())

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

    storeNormalizedCoords(index, xx, yy)
  }

  private def storeNormalizedCoords(index: Int, xx: Double, yy: Double) = {
    canvas.storeCoords(index, xx, yy)
    notifyListeners(_.canvas.storeCoords(index, xx, yy))
    storeCoordsInIndexMap(index, xx, yy)
  }

  private def storeCoordsInIndexMap(index: Int, xx: Double, yy: Double) = {
    indexMap.storeCoords(index, xx, yy)
  }

  private def updateCanvasSize(): Unit = {
    canvas.updateCanvasSize(storage.imageSize, zoom)
    updateLocation()
  }

  private def updateAfterDraw(): Unit = {
    undoManager.append(storage.cumulativeChange.done(EditMode.currentMode.tooltipText, this))
    redraw(false)
  }

  def applyEffect(effect: Effect): Unit = {
    effect.action(storage)

    updateAfterDraw()
  }

  private def updateLocation(): Unit = {
    canvas.updateLocation(panX, panY)
  }

  def save: Boolean = storage.save

  def setSaveLocation(location: SaveLocation): Unit = {
    storage.saveLocation = location
  }

  def changed: Boolean = storage.hasChanged
  def changedProperty: ReadOnlyBooleanProperty = storage.hasChangedProperty

  override def onPixelChanged(coords: Coord): Unit = {
    drawTriangle(coords, false)
  }
}

class TriImageTooltip(storage: ImageStorage) extends Tooltip {
  text <== storage.infoText
}
