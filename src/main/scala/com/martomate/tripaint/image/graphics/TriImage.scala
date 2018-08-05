package com.martomate.tripaint.image.graphics

import com.martomate.tripaint.image.content.{ImageChangeListener, ImageContent}
import com.martomate.tripaint.image.coords.TriangleCoords
import com.martomate.tripaint.image.effects.Effect
import com.martomate.tripaint.image.storage._
import com.martomate.tripaint.undo.UndoManager
import scalafx.beans.property._
import scalafx.scene.SnapshotParameters
import scalafx.scene.image.Image
import scalafx.scene.layout.Pane
import scalafx.scene.paint.Color

object TriImage {
  val previewSize = 64

  def apply(content: ImageContent, imagePane: ImageGridView) = new TriImage(content, imagePane)
}

class TriImage private(val content: ImageContent, val imagePane: ImageGridView) extends Pane with ITriImage {

  private def panX = content.coords.xOff * imagePane.sideLength
  private def panY = content.coords.yOff * imagePane.sideLength
  private def zoom = imagePane.zoom

  private def storage: ImageStorage = content.storage

  def changed: Boolean = content.changeTracker.changed
  def changedProperty: ReadOnlyBooleanProperty = content.changeTracker.changedProperty

  private val canvas: TriImageActualCanvas = new TriImageActualCanvas(imagePane.imageSize)

  content.changeTracker.addListener(this)
  children add canvas
  if (content.coords.x % 2 != 0) canvas.rotate() += 180
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

  override protected def drawTriangle(coords: TriangleCoords): Unit = drawTriangle(coords, doIndexMapping = false)

  private[image] def drawTriangle(coords: TriangleCoords, doIndexMapping: Boolean, strokeInstead: Boolean = false): Unit = {
    val yp = coords.y
    val xp = coords.x * 0.5 - (yp - storage.imageSize + 1) * 0.5

    storeAllCoords(xp, yp, coords.x % 2 == 1)

    canvas.drawTriangle(storage(coords), strokeInstead)

    if (doIndexMapping) indexMap.performIndexMapping(coords)
  }

  override def redraw(): Unit = redraw(false)

  def redraw(doIndexMapping: Boolean): Unit = {
    canvas.clearCanvas()

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

  private def storeNormalizedCoords(index: Int, xx: Double, yy: Double): Unit = {
    canvas.storeNormalizedCoords(index, xx, yy)
    storeCoordsInIndexMap(index, xx, yy)
  }

  private def storeCoordsInIndexMap(index: Int, xx: Double, yy: Double): Unit = {
    indexMap.storeCoords(index, xx, yy)
  }

  private def updateCanvasSize(): Unit = {
    canvas.updateCanvasSize(storage.imageSize, zoom)
    updateLocation()
  }

  private def updateAfterDraw(): Unit = {
//    undoManager.append(storage.cumulativeChange.done(EditMode.currentMode.tooltipText, this))
    // TODO: reintroduce the undo manager stuff somewhere else
    content.changeTracker.tellListenersAboutBigChange()
  }

  def applyEffect(effect: Effect): Unit = {
    effect.action(storage)

    updateAfterDraw()
  }

  private def updateLocation(): Unit = {
    canvas.updateLocation(panX, panY)
  }
}
