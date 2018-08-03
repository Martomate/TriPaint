package com.martomate.tripaint.image

import java.awt.image.BufferedImage

import com.martomate.tripaint.image.coords.TriangleCoords
import com.martomate.tripaint.image.effects.Effect
import com.martomate.tripaint.image.storage._
import com.martomate.tripaint.undo.UndoManager
import com.martomate.tripaint.{ImagePane, Listenable}
import scalafx.beans.property._
import scalafx.scene.SnapshotParameters
import scalafx.scene.canvas.Canvas
import scalafx.scene.image.Image
import scalafx.scene.layout.Pane
import scalafx.scene.paint.Color

object TriImage {
  val previewSize = 64

  def apply(coords: TriImageCoords, content: ImageContent, imagePane: ImagePane) =
    new TriImage(coords, content, imagePane)
}

trait IndexMapper {
  def coordsAt(x: Double, y: Double): TriangleCoords
}

class IndexMap(canvas: Canvas, init_zoom: Double) extends IndexMapper {
  val xInt:  Array[Int] = new Array(3)
  val yInt:  Array[Int] = new Array(3)
  val image: BufferedImage = new BufferedImage(
    Math.ceil(canvas.width()  / init_zoom * 3).toInt,
    Math.ceil(canvas.height() / init_zoom * 3).toInt,
    BufferedImage.TYPE_INT_RGB
  )

  def coordsAt(x: Double, y: Double): TriangleCoords = {
    val xx = (x / canvas.width()  * image.getWidth ).toInt
    val yy = (y / canvas.height() * image.getHeight).toInt

    if (xx >= 0 && xx < image.getWidth() && yy >= 0 && yy < image.getHeight())
      TriangleCoords.fromInt((image.getRGB(xx, yy) & 0xffffff) - 1)
    else null
  }

  def performIndexMapping(coords: TriangleCoords): Unit = {
    val indexGraphics = image.getGraphics
    val indexColor = new java.awt.Color(coords.toInt + 1)
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

class TriImage private(val coords: TriImageCoords, val content: ImageContent, val imagePane: ImagePane)
  extends Pane
    with ImageChangeListener
    with Listenable[TriImageView] {

  private def panX = coords.xOff * imagePane.sideLength
  private def panY = coords.yOff * imagePane.sideLength
  private def zoom = imagePane.globalZoom

  def storage: ImageStorage = content.storage

  val canvas: TriImageActualCanvas = new TriImageActualCanvas(imagePane.imageSize)

  def editableProperty = content.editableProperty
  def editable = content.editable

  content.changeTracker.addListener(this)
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

  def drawAt(coords: TriangleCoords, color: Color): Unit = drawAtCoords(coords, color)

  def drawAtCoords(coords: TriangleCoords, color: Color): Unit = {
    if (storage.contains(coords)) {
      storage(coords) = color
    } else println("outside!!")
  }

  def coordsAt(x: Double, y: Double): TriangleCoords = {
    val pt = canvas.sceneToLocal(x, y)
    indexMap.coordsAt(pt.getX, pt.getY)
  }

  private[image] def drawTriangle(coords: TriangleCoords, doIndexMapping: Boolean, strokeInstead: Boolean = false): Unit = {
    val yp = coords.y
    val xp = coords.x * 0.5 - (yp - storage.imageSize + 1) * 0.5

    storeAllCoords(xp, yp, coords.x % 2 == 1)

    canvas.drawTriangle(storage(coords), strokeInstead)
    notifyListeners(_.canvas.drawTriangle(storage(coords), strokeInstead))

    if (doIndexMapping) indexMap.performIndexMapping(coords)
  }

  def redraw(doIndexMapping: Boolean): Unit = {
    canvas.clearCanvas()
    notifyListeners(_.canvas.clearCanvas())

    for (y <- 0 until storage.imageSize) {
      for (x <- 0 until y * 2 + 1) {
        drawTriangle(TriangleCoords(x, y), doIndexMapping, strokeInstead = true)
      }
    }

    for (y <- 0 until storage.imageSize) {
      for (x <- 0 until y * 2 + 1) {
        drawTriangle(TriangleCoords(x, y), doIndexMapping)
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
//    undoManager.append(storage.cumulativeChange.done(EditMode.currentMode.tooltipText, this))
    // TODO: reintroduce the undo manager stuff somewhere else
    redraw(false)
  }

  def applyEffect(effect: Effect): Unit = {
    effect.action(storage)

    updateAfterDraw()
  }

  private def updateLocation(): Unit = {
    canvas.updateLocation(panX, panY)
  }

  def changed: Boolean = content.changeTracker.changed
  def changedProperty: ReadOnlyBooleanProperty = content.changeTracker.changedProperty

  override def onPixelChanged(coords: TriangleCoords, from: Color, to: Color): Unit = {
    drawTriangle(coords, false)
  }

  override def onImageReplaced(oldImage: ImageStorage, newImage: ImageStorage): Unit = redraw(false)
}
