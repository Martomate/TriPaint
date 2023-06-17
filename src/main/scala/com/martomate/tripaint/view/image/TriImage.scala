package com.martomate.tripaint.view.image

import com.martomate.tripaint.model.Color
import com.martomate.tripaint.model.image.content.{CumulativeImageChange, GridCell}
import com.martomate.tripaint.model.coords.TriangleCoords
import com.martomate.tripaint.model.image.ImageStorage
import javafx.scene.input.{MouseEvent, ScrollEvent}
import scalafx.beans.property.ReadOnlyBooleanProperty
import scalafx.scene.layout.Pane

class TriImage(val content: GridCell, init_zoom: Double):
  val pane: Pane = new Pane()

  private val indexMap = new IndexMap(storage.imageSize)

  private val canvas: TriImageCanvas =
    new TriImageCanvas((storage.imageSize * 2 + 1) * init_zoom, storage.imageSize)

  private val cumulativeImageChange = new CumulativeImageChange

  content.trackChanges(this.onImageChanged _)
  pane.children.add(canvas)

  if content.coords.x % 2 != 0 then canvas.rotate() += 180
  updateCanvasSize(init_zoom)

  redraw()

  private def storage: ImageStorage = content.storage

  private def panX(zoom: Double) = content.coords.centerX * (storage.imageSize * 2 + 1) * zoom
  private def panY(zoom: Double) = content.coords.centerY * (storage.imageSize * 2 + 1) * zoom

  def onStoppedDrawing(): Unit =
    content.appendChange(cumulativeImageChange.done("draw", content.storage))

  def onZoom(zoom: Double): Unit =
    updateCanvasSize(zoom)
    redraw()

  def drawAt(coords: TriangleCoords, color: Color): Unit =
    if storage.contains(coords) then
      cumulativeImageChange.addChange(coords, storage.getColor(coords), color)
      storage.setColor(coords, color)

  /** @param x
    *   the x coordinate in scene space
    * @param y
    *   the y coordinate in scene space
    * @return
    *   the `TriangleCoords` of the point if it lies inside the triangle, null otherwise
    */
  def coordsAt(x: Double, y: Double): TriangleCoords =
    val pt = canvas.sceneToLocal(x, y)
    indexMap.coordsAt(pt.getX / canvas.width(), pt.getY / canvas.height())

  private def updateCanvasSize(zoom: Double): Unit =
    canvas.setCanvasSize((storage.imageSize * 2 + 1) * zoom)
    canvas.setCanvasLocationUsingCenter(panX(zoom), panY(zoom))

  private def drawTriangle(coords: TriangleCoords): Unit =
    canvas.drawTriangle(coords, storage.getColor(coords), storage)

  private def redraw(): Unit =
    canvas.clearCanvas()
    canvas.redraw(storage)

  def onImageChanged(event: GridCell.Event): Unit =
    import GridCell.Event.*
    event match
      case PixelChanged(coords, _, _) =>
        drawTriangle(coords)
      case ImageChangedALot =>
        redraw()
