package com.martomate.tripaint.view.image

import com.martomate.tripaint.model.image.content.{CumulativeImageChange, ImageContent}
import com.martomate.tripaint.model.coords.TriangleCoords
import com.martomate.tripaint.model.image.storage.ImageStorage
import javafx.scene.input.{MouseEvent, ScrollEvent}
import scalafx.beans.property.ReadOnlyBooleanProperty
import scalafx.scene.layout.Pane
import scalafx.scene.paint.Color

class TriImageImpl private(val content: ImageContent, val imagePane: ImageGridView) extends Pane with TriImage {
  def pane: Pane = this

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

  private val indexMap = new IndexMap(storage.imageSize)

  redraw()

  def onMouseReleased(e: MouseEvent): Unit = {
    if (!e.isConsumed) {
      finishCumulativeChange()
    }
  }

  def onScroll(e: ScrollEvent): Unit = {
    if (e.isControlDown) {
      updateCanvasSize()

      redraw()
    }
  }

  private val undoManager = content.undoManager
  def undo(): Unit = content.undo()
  def redo(): Unit = content.redo()

  private val cumulativeImageChange = new CumulativeImageChange
  def finishCumulativeChange(): Unit =
    undoManager.append(cumulativeImageChange.done("draw", content))

  def drawAt(coords: TriangleCoords, color: Color): Unit = {
    if (storage.contains(coords)) {
      cumulativeImageChange.addChange(coords, storage(coords), color)
      storage(coords) = color
    } else println("outside!!")
  }

  def coordsAt(x: Double, y: Double): TriangleCoords = {
    val pt = canvas.sceneToLocal(x, y)
    indexMap.coordsAt(pt.getX / canvas.width(), pt.getY / canvas.height())
  }

  override protected def drawTriangle(coords: TriangleCoords): Unit = canvas.drawTriangle(coords, storage(coords), storage)

  override def redraw(): Unit = {
    canvas.clearCanvas()

    canvas.redraw(storage)
  }

  private def updateCanvasSize(): Unit = {
    canvas.updateCanvasSize(storage.imageSize, zoom)
    canvas.updateLocation(panX, panY)
  }

  override def relocate(x: Double, y: Double): Unit = {
    super.relocate(x, y)
  }
}

object TriImageImpl {
  def apply(content: ImageContent, imagePane: ImageGridView) = new TriImageImpl(content, imagePane)
}
