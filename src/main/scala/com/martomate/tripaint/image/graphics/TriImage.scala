package com.martomate.tripaint.image.graphics

import com.martomate.tripaint.image.{CumulativeImageChange, ImageChange}
import com.martomate.tripaint.image.content.ImageContent
import com.martomate.tripaint.image.coords.TriangleCoords
import com.martomate.tripaint.image.effects.Effect
import com.martomate.tripaint.image.storage._
import com.martomate.tripaint.undo.UndoManager
import javafx.scene.input.MouseEvent
import scalafx.beans.property._
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

  private val canvas: TriImageActualCanvas = new TriImageActualCanvas(imagePane.imageSize, imagePane.imageSize)

  content.changeTracker.addListener(this)
  children add canvas
  if (content.coords.x % 2 != 0) canvas.rotate() += 180
  updateCanvasSize()

  private val indexMap = new IndexMap(canvas, zoom, storage.imageSize)

  redraw(true)

  def onMouseReleased(e: MouseEvent): Unit = {
    if (!e.isConsumed) {
      finishCumulativeChange()
      updateAfterDraw()
    }
  }

  onScroll = e => {
    if (e.isControlDown) {
      updateCanvasSize()

      redraw(true)
    }
  }

  private val undoManager = new UndoManager
  def undo(): Unit = undoManager.undo()
  def redo(): Unit = undoManager.redo()

  private val cumulativeImageChange = new CumulativeImageChange
  def finishCumulativeChange(): Unit =
    undoManager.append(cumulativeImageChange.done("draw", this))

  def drawAt(coords: TriangleCoords, color: Color): Unit = {
    if (storage.contains(coords)) {
      cumulativeImageChange.addChange(coords, storage(coords), color)
      storage(coords) = color
    } else println("outside!!")
  }

  def coordsAt(x: Double, y: Double): TriangleCoords = {
    val pt = canvas.sceneToLocal(x, y)
    indexMap.coordsAt(pt.getX, pt.getY)
  }

  override protected def drawTriangle(coords: TriangleCoords): Unit = drawTriangleImpl(coords, doIndexMapping = false)

  private def drawTriangleImpl(coords: TriangleCoords, doIndexMapping: Boolean, strokeInstead: Boolean = false): Unit = {
    canvas.drawTriangle(coords, storage(coords), strokeInstead)

    if (doIndexMapping) indexMap.drawTriangle(coords)
  }

  override def redraw(): Unit = redraw(false)

  private def redraw(doIndexMapping: Boolean): Unit = {
    canvas.clearCanvas()

    for (c <- storage.allPixels) {
      drawTriangleImpl(c, doIndexMapping, strokeInstead = true)
    }

    for (c <- storage.allPixels) {
      drawTriangleImpl(c, doIndexMapping)
    }
  }

  private def updateCanvasSize(): Unit = {
    canvas.updateCanvasSize(storage.imageSize, zoom)
    updateLocation()
  }

  // TODO: move the undo manager stuff somewhere else
  private def updateAfterDraw(): Unit = {
    content.changeTracker.tellListenersAboutBigChange()
  }

  def applyEffect(effect: Effect): Unit = {
    effect.action(storage)

    finishCumulativeChange()
    updateAfterDraw()
  }

  private def updateLocation(): Unit = {
    canvas.updateLocation(panX, panY)
  }
}
